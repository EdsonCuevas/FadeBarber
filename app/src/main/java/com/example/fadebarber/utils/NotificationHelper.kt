package com.example.fadebarber.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.fadebarber.MainActivity
import com.example.fadebarber.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging

object NotificationHelper {

    private const val TAG = "NotificationHelper"
    private const val CHANNEL_ID = "fade_barber_notifications"
    private const val CHANNEL_NAME = "🔔 FadeBarber Actualizaciones"
    private const val CHANNEL_DESCRIPTION = "Recibe notificaciones cuando actualices tu perfil en FadeBarber ✂️"

    // Guardar token del usuario en la base de datos
    fun saveUserToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            // Obtener nuevo token FCM
            val token = task.result
            Log.d(TAG, "FCM Registration Token: $token")

            // Guardar el token en Realtime Database
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            userId?.let {
                val database = FirebaseDatabase.getInstance().getReference("UserTokens")
                database.child(it).setValue(token)
                    .addOnSuccessListener {
                        Log.d(TAG, "Token saved successfully")
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Failed to save token", e)
                    }
            }
        }
    }

    // Función helper para convertir drawable a bitmap
    private fun getBitmapFromDrawable(context: Context, drawableId: Int): Bitmap? {
        return try {
            val drawable = ContextCompat.getDrawable(context, drawableId)

            if (drawable is BitmapDrawable) {
                return drawable.bitmap
            }

            val bitmap = Bitmap.createBitmap(
                drawable!!.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )

            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)

            bitmap
        } catch (e: Exception) {
            Log.e(TAG, "Error creating bitmap from drawable", e)
            null
        }
    }

    // Enviar notificación local real
    fun sendProfileUpdateNotification(context: Context, userName: String, updateType: String) {
        Log.d(TAG, "Sending real notification for: $updateType")

        val title = getNotificationTitle(updateType)
        val body = getNotificationBody(userName, updateType)

        Log.d(TAG, "Creating notification: $title - $body")

        createNotificationChannel(context)
        showNotification(context, title, body)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel created")
        }
    }

    private fun showNotification(context: Context, title: String, body: String) {
        // Intent para abrir la app cuando se toque la notificación
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        // Sonido de notificación
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        // Crear la notificación con estilo expandido y más atractiva
        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo) // Si no tienes ic_notification, usa logo
            .setLargeIcon(getBitmapFromDrawable(context, R.drawable.logo)) // Logo a color más grande
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(body)
                    .setBigContentTitle(title)
                    .setSummaryText("FadeBarber")
            )
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setColor(0xFF0A66C2.toInt()) // Color azul de tu app
            .setLights(0xFF0A66C2.toInt(), 1000, 1000) // Luces LED azules
            .setVibrate(longArrayOf(0, 300, 200, 300)) // Patrón de vibración personalizado
            .setOnlyAlertOnce(false) // Permitir alertas múltiples
            .setGroupSummary(false)
            .setShowWhen(true)
            .setWhen(System.currentTimeMillis())

        // Mostrar la notificación
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = System.currentTimeMillis().toInt()

        try {
            notificationManager.notify(notificationId, notificationBuilder.build())
            Log.d(TAG, "Notification displayed successfully with ID: $notificationId")
        } catch (e: Exception) {
            Log.e(TAG, "Error showing notification", e)
        }
    }

    // === Notificación de cita agendada ===
    fun sendAppointmentBookedNotification(
        context: Context,
        userName: String,
        date: String,
        time: String,
        barberName: String? = null
    ) {
        val title = "📅 Cita Agendada"
        val body = buildString {
            append("$userName, tu cita ha sido agendada para $date a las $time")
            if (!barberName.isNullOrBlank()) {
                append(" con $barberName")
            }
        }
        createNotificationChannel(context)
        showNotification(context, title, body)
    }

    private fun getNotificationTitle(updateType: String): String {
        return when (updateType) {
            "name_change" -> "🎉 ¡Perfil Actualizado!"
            "email_change" -> "📧 ¡Email Actualizado!"
            "phone_change" -> "📱 ¡Teléfono Actualizado!"
            "password_change" -> "🔐 ¡Seguridad Actualizada!"
            "profile_update" -> "✨ ¡Perfil Mejorado!"
            else -> "🔄 Actualización Completa"
        }
    }

    private fun getNotificationBody(userName: String, updateType: String): String {
        return when (updateType) {
            "name_change" -> "¡Hola $userName! Tu nombre se ha actualizado exitosamente. ¡Tu perfil se ve genial! 🌟"
            "email_change" -> "Tu correo electrónico se actualizó correctamente. ¡Mantén tu cuenta segura! 🛡️"
            "phone_change" -> "Tu número de teléfono se ha actualizado. ¡Excelente trabajo manteniendo tu información al día! 📲"
            "password_change" -> "Tu contraseña se actualizó con éxito. ¡Tu cuenta está más segura que nunca! 🔒"
            "profile_update" -> "¡Perfecto! Tu información se ha actualizado correctamente en FadeBarber ✂️"
            else -> "¡Genial! Todos los cambios se guardaron exitosamente 🎯"
        }
    }
}