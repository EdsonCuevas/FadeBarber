package com.example.fadebarber.utils

import android.R.attr.apiKey
import com.example.fadebarber.services.SecretConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Notifica al cliente cuando su cita es reagendada por el empleado
 * @param appointmentId ID de la cita
 * @param nuevaFecha Nueva fecha en formato YYYY-MM-DD
 * @param nuevaHora Nueva hora en formato HH:mm
 * @param employeeName Nombre del empleado
 */
suspend fun notificarClienteReagendamiento(
    appointmentId: String,
    nuevaFecha: String,
    nuevaHora: String,
    employeeName: String,
    apiUrl: String = "https://api-fadebarber.vercel.app"
): Boolean {
    return withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()

            // Construir el JSON con los datos
            val jsonBody = JSONObject().apply {
                put("appointmentId", appointmentId)
                put("nuevaFecha", nuevaFecha)
                put("nuevaHora", nuevaHora)
                put("employeeName", employeeName)
            }

            val requestBody = jsonBody.toString()
                .toRequestBody("application/json; charset=utf-8".toMediaType())

            val request = Request.Builder()
                .url("$apiUrl/notificar-reagendar-cita")
                .addHeader("x-api-key", SecretConfig.get("API_KEY"))
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful) {
                println("✅ Notificación de reagendamiento enviada: $responseBody")
                true
            } else {
                println("⚠️ Error al notificar reagendamiento (${response.code}): $responseBody")
                false
            }
        } catch (e: Exception) {
            println("❌ Excepción al notificar reagendamiento: ${e.message}")
            e.printStackTrace()
            false
        }
    }
}
