package com.example.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat


actual class NotificationPlatformHelper actual constructor() {
    
    private val CHANNEL_ID = "untisneo_channel"

    actual fun initChannels() {
        val context = AppGlobals.appContext ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "UntisNeo Benachrichtigungen"
            val descriptionText = "Benachrichtigungen für Stundenplanänderungen und Hausaufgaben"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    actual fun showNotification(title: String, content: String) {
        val context = AppGlobals.appContext ?: return
        
        // Android requires a small icon, we fallback to android.R.drawable.ic_dialog_info since we might not have R.mipmap.ic_launcher easily accessible without importing the specific R package
        val resId = context.resources.getIdentifier("ic_launcher", "mipmap", context.packageName)
        val iconId = if (resId != 0) resId else android.R.drawable.ic_dialog_info

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(iconId)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Random ID to prevent replacing existing notifications unless intended
        val notificationId = (System.currentTimeMillis() % 10000).toInt()
        notificationManager.notify(notificationId, builder.build())
    }
}
