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
 * Notifica al cliente cuando el estado de su cita cambia
 * @param appointmentId ID de la cita
 * @param nuevoEstado 2 = En proceso, 3 = Finalizada, 4 = Cancelada
 * @param employeeName Nombre del empleado
 */
suspend fun notificarClienteCambioEstado(
    appointmentId: String,
    nuevoEstado: Int,
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
                put("nuevoEstado", nuevoEstado)
                put("employeeName", employeeName)
            }

            val requestBody = jsonBody.toString()
                .toRequestBody("application/json; charset=utf-8".toMediaType())

            val request = Request.Builder()
                .url("$apiUrl/notificar-cliente-estado")
                .addHeader("x-api-key", SecretConfig.get("API_KEY"))
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful) {
                println("✅ Notificación al cliente enviada: $responseBody")
                true
            } else {
                println("⚠️ Error al notificar cliente (${response.code}): $responseBody")
                false
            }
        } catch (e: Exception) {
            println("❌ Excepción al notificar cliente: ${e.message}")
            e.printStackTrace()
            false
        }
    }
}
