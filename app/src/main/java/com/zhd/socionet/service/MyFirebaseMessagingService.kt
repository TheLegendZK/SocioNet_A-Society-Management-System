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
import com.zhd.socionet.ui.navigation.Routes
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
                val isForThisFlat = hostFlatId != null && profile?.assignedFlatId == hostFlatId
                
                if (!isForThisUser && !isForThisFlat) {
                    // Ignore notification meant for a different user/device
                    return@launch
                }
            }

            // Check if message contains a notification payload.
            remoteMessage.notification?.let {
                showNotification(it.title ?: "SocioNet", it.body ?: "New Alert")
            } ?: run {
                // Also handle data-only payload
                val title = remoteMessage.data["title"] ?: "SocioNet"
                val body = remoteMessage.data["body"] ?: "New Alert"
                if (remoteMessage.data.containsKey("entryId")) {
                    showNotification(title, body)
                }
            }
        }
    }

    private suspend fun showNotification(title: String, message: String) {
        val channelId = "socionet_alerts"
        val notificationId = System.currentTimeMillis().toInt()

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        // Determine target route if user is logged in
        val userId = authRepository.getCurrentUserId()
        if (userId != null) {
            val profile = authRepository.getUserProfile(userId)
            if (profile != null && !profile.assignedFlatId.isNullOrBlank()) {
                // Navigate to Approvals tab (index 1)
                val route = Routes.buildResidentCreatePass(profile.assignedFlatId, userId, tab = 1)
                intent.putExtra("navigation_route", route)
            }
        }

        // Open the app when clicked
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert) // Replace with your app icon
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Create Channel for Android O+
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
