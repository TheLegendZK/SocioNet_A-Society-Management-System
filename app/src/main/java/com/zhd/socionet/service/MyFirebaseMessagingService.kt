package com.zhd.socionet.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.zhd.socionet.MainActivity
import com.zhd.socionet.data.repository.AuthRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var authRepository: AuthRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val targetUserId = remoteMessage.data["targetUserId"]
        val hostFlatId = remoteMessage.data["hostFlatId"]

        serviceScope.launch {
            val currentUserId = authRepository.getCurrentUserId() ?: return@launch
            val profile = authRepository.getUserProfile(currentUserId)
            
            // SECURITY CHECK: Only show notification if it's targeted to this user or their flat
            if (!targetUserId.isNullOrBlank() || !hostFlatId.isNullOrBlank()) {
                val isForThisUser = targetUserId == currentUserId
                val isForThisFlat = hostFlatId != null && profile?.flatId == hostFlatId
                
                if (!isForThisUser && !isForThisFlat) {
                    return@launch
                }
            }

            // Extract display info
            val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "SocioNet"
            val body = remoteMessage.notification?.body ?: remoteMessage.data["body"] ?: "New Alert"
            
            showNotification(title, body, remoteMessage.data)
        }
    }

    private suspend fun showNotification(title: String, message: String, data: Map<String, String>) {
        val channelId = "socionet_alerts"
        val notificationId = System.currentTimeMillis().toInt()

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            // Add all data to intent extras so MainActivity can handle it
            data.forEach { (key, value) -> putExtra(key, value) }
        }

        val pendingIntent = PendingIntent.getActivity(
            this, notificationId, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Security Alerts",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        withContext(Dispatchers.Main) {
            notificationManager.notify(notificationId, notificationBuilder.build())
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        serviceScope.launch {
            authRepository.updateFcmToken(token)
        }
    }
}
