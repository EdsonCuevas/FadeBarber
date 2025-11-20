package com.example.fadebarber.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.fadebarber.MainActivity
import com.example.fadebarber.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCM_Service"
        const val CHANNEL_ID = "fade_barber_notifications"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d(TAG, "From: ${remoteMessage.from}")

        // Verificar si el mensaje tiene datos
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            handleDataMessage(remoteMessage.data)
        }

        // Verificar si el mensaje tiene notificación
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            sendNotification(it.title ?: "FadeBarber", it.body ?: "Nueva actualización")
        }
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        // Aquí puedes enviar el token a tu servidor si tienes uno
        // sendTokenToServer(token)
    }

    private fun handleDataMessage(data: Map<String, String>) {
        val body = data["body"] ?: "Hay una nueva actualización"
        val type = data["type"] ?: "general"

        when (type) {
            "appointment_booked" -> {
                val userName = data["userName"] ?: "Usuario"
                val date = data["date"] ?: "fecha"
                val time = data["time"] ?: "hora"
                val barberName = data["barberName"]
                val tail = if (!barberName.isNullOrBlank()) " con $barberName" else ""
                sendNotification(
                    title = "Cita Agendada",
                    messageBody = "$userName, tu cita ha sido agendada para $date a las $time$tail"
                )
            }
            "profile_update" -> {
                val userName = data["userName"] ?: "Usuario"
                sendNotification(
                    title = "Perfil Actualizado",
                    messageBody = "$userName ha actualizado su perfil"
                )
            }
            "name_change" -> {
                val userName = data["userName"] ?: "Usuario"
                sendNotification(
                    title = "Nombre Actualizado",
                    messageBody = "Tu nombre ha sido actualizado a: $userName"
                )
            }
            "password_change" -> {
                sendNotification(
                    title = "Seguridad",
                    messageBody = "Tu contraseña ha sido actualizada correctamente"
                )
            }
            "email_change" -> {
                sendNotification(
                    title = "Email Actualizado",
                    messageBody = "Tu correo electrónico ha sido actualizado"
                )
            }
            "phone_change" -> {
                sendNotification(
                    title = "Teléfono Actualizado",
                    messageBody = "Tu número de teléfono ha sido actualizado"
                )
            }
            else -> {
                Log.d(TAG, "Message Notification Body: $body")
            }
        }
    }

    private fun sendNotification(title: String, messageBody: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = CHANNEL_ID
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.logo) // Asegúrate de tener este ícono
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Para Android O y superior, crear canal de notificación
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "FadeBarber Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de actualización de perfil"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
}