package com.example.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.model.SocialNotification
import com.example.model.SocialPlatform

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_WHATSAPP = "omni_channel_whatsapp"
        const val CHANNEL_INSTAGRAM = "omni_channel_instagram"
        const val CHANNEL_FACEBOOK = "omni_channel_facebook"
        const val CHANNEL_MESSENGER = "omni_channel_messenger"
        const val CHANNEL_GENERAL = "omni_channel_general"
        const val EXTRA_TARGET_PLATFORM = "extra_target_platform"
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val channels = listOf(
                NotificationChannel(
                    CHANNEL_WHATSAPP,
                    "WhatsApp Alerts",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Incoming WhatsApp messages and call reminders"
                    enableLights(true)
                    lightColor = Color.GREEN
                    enableVibration(true)
                },
                NotificationChannel(
                    CHANNEL_INSTAGRAM,
                    "Instagram Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Instagram DMs, likes, mentions and comments"
                    enableLights(true)
                    lightColor = Color.MAGENTA
                    enableVibration(true)
                },
                NotificationChannel(
                    CHANNEL_FACEBOOK,
                    "Facebook Updates",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Facebook friend requests, tag notifications, and news"
                    enableLights(true)
                    lightColor = Color.BLUE
                    enableVibration(true)
                },
                NotificationChannel(
                    CHANNEL_MESSENGER,
                    "Messenger Chats",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Direct Messenger chats and group ping alerts"
                    enableLights(true)
                    lightColor = Color.CYAN
                    enableVibration(true)
                },
                NotificationChannel(
                    CHANNEL_GENERAL,
                    "OmniSocial System & Sync",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Cloud sync updates, security alerts, and system status"
                }
            )

            channels.forEach { manager.createNotificationChannel(it) }
        }
    }

    private fun getChannelIdForPlatform(platformId: String): String {
        return when (platformId.lowercase()) {
            SocialPlatform.WHATSAPP.id -> CHANNEL_WHATSAPP
            SocialPlatform.INSTAGRAM.id -> CHANNEL_INSTAGRAM
            SocialPlatform.FACEBOOK.id -> CHANNEL_FACEBOOK
            SocialPlatform.MESSENGER.id -> CHANNEL_MESSENGER
            else -> CHANNEL_GENERAL
        }
    }

    fun showNotification(notification: SocialNotification) {
        val channelId = getChannelIdForPlatform(notification.platformId)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_TARGET_PLATFORM, notification.platformId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notification.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val platform = SocialPlatform.entries.find { it.id == notification.platformId }
        val platformLabel = platform?.displayName ?: "OmniSocial"

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("[$platformLabel] ${notification.title}")
            .setContentText(notification.snippet)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notification.snippet))
            .setPriority(
                if (notification.platformId == SocialPlatform.WHATSAPP.id || notification.platformId == SocialPlatform.MESSENGER.id) {
                    NotificationCompat.PRIORITY_HIGH
                } else {
                    NotificationCompat.PRIORITY_DEFAULT
                }
            )
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            if (notificationManager.areNotificationsEnabled()) {
                val notifId = (notification.id.toInt().takeIf { it != 0 } ?: (System.currentTimeMillis() % 10000).toInt())
                notificationManager.notify(notifId, builder.build())
            }
        } catch (_: SecurityException) {
            // Handled gracefully if notification permission is not granted
        }
    }
}
