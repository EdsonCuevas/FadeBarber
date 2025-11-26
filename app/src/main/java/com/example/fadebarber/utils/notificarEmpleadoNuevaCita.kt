package com.example.fadebarber.utils

import android.R.attr.apiKey
import com.example.fadebarber.data.model.AppointmentClientData
import com.example.fadebarber.services.SecretConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Envía una notificación al empleado sobre una nueva cita agendada
 */
suspend fun notificarEmpleadoNuevaCita(
    appointmentData: AppointmentClientData,
    apiUrl: String = "https://api-fadebarber.vercel.app"
): Boolean {
    return withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()

            // Construir el JSON con los datos de la cita
            val jsonBody = JSONObject().apply {
                put("idEmployee", appointmentData.idEmployee)
                put("nameClient", appointmentData.nameClient)
                put("dateAppointment", appointmentData.dateAppointment)
                put("timeAppointment", appointmentData.timeAppointment)
                put("phoneNumberClient", appointmentData.phoneNumberClient ?: "")

                // Convertir array de serviceId a JSONArray
                val serviceArray = JSONArray()
                appointmentData.serviceId.forEach { serviceArray.put(it) }
                put("serviceId", serviceArray)

                // 🆕 Agregar array de idPromotion
                val promotionArray = JSONArray()
                appointmentData.idPromotion.forEach { promotionArray.put(it) }
                put("idPromotion", promotionArray)
            }

            val requestBody = jsonBody.toString()
                .toRequestBody("application/json; charset=utf-8".toMediaType())

            val request = Request.Builder()
                .url("$apiUrl/notificar-nueva-cita")
                .addHeader("x-api-key", SecretConfig.get("API_KEY"))
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful) {
                println("✅ Notificación al empleado enviada: $responseBody")
                true
            } else {
                println("⚠️ Error al notificar empleado (${response.code}): $responseBody")
                // No fallar la cita si falla la notificación
                false
            }
        } catch (e: Exception) {
            println("❌ Excepción al notificar empleado: ${e.message}")
            e.printStackTrace()
            // No fallar la cita si falla la notificación
            false
        }
    }
}