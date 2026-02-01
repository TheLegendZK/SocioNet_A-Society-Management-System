package com.zhd.socionet.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.zhd.socionet.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            showNotification(it.title ?: "SocioNet", it.body ?: "New Alert")
        }

        // Also handle data payload (e.g. for silent updates)
        if (remoteMessage.data.isNotEmpty()) {
            val title = remoteMessage.data["title"]
            val body = remoteMessage.data["body"]
            if (title != null && body != null) {
                showNotification(title, body)
            }
        }
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "socionet_alerts"
        val notificationId = System.currentTimeMillis().toInt()

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        // Open the app when clicked
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert) // Replace with your app icon
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create Channel for Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Security Alerts",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    override fun onNewToken(token: String) {
        // Send this token to Firestore (users collection) so the backend knows where to send alerts
        // In a real app, inject Repository here and updateProfile(token)
    }
}