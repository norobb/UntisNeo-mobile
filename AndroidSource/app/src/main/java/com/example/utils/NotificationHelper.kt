package com.example.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity

object NotificationHelper {
    private const val CHANNEL_HW_ID = "homework_alerts_channel"
    private const val CHANNEL_TIMETABLE_ID = "timetable_changes_channel"

    private const val HW_NOTIFICATION_ID = 1001
    private const val TIMETABLE_NOTIFICATION_ID = 1002

    fun initChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nameHw = "Hausaufgaben-Erinnerungen"
            val descriptionTextHw = "Benachrichtigungen über fällige oder neue Hausaufgaben"
            val importanceHw = NotificationManager.IMPORTANCE_DEFAULT
            val channelHw = NotificationChannel(CHANNEL_HW_ID, nameHw, importanceHw).apply {
                description = descriptionTextHw
            }

            val nameTimetable = "Stundenplan-Änderungen"
            val descriptionTextTimetable = "Sofortige Alerts bei Vertretungen, Ausfällen oder Raumänderungen"
            val importanceTimetable = NotificationManager.IMPORTANCE_HIGH
            val channelTimetable = NotificationChannel(CHANNEL_TIMETABLE_ID, nameTimetable, importanceTimetable).apply {
                description = descriptionTextTimetable
            }

            // Register the channels with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channelHw)
            notificationManager.createNotificationChannel(channelTimetable)
            Log.d("NotificationHelper", "Notification Channels initialized successfully")
        }
    }

    fun sendHomeworkNotification(context: Context, title: String, message: String) {
        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent: PendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, CHANNEL_HW_ID)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm) // System standard fallback icon
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
            Log.d("NotificationHelper", "Homework notification sent: $title - $message")
        } catch (e: Exception) {
            Log.e("NotificationHelper", "Error sending homework notification", e)
        }
    }

    fun sendTimetableChangeNotification(context: Context, title: String, message: String) {
        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent: PendingIntent = PendingIntent.getActivity(
                context, 1, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, CHANNEL_TIMETABLE_ID)
                .setSmallIcon(android.R.drawable.stat_notify_sync) // System standard status fallback icon
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
            Log.d("NotificationHelper", "Timetable change notification sent: $title - $message")
        } catch (e: Exception) {
            Log.e("NotificationHelper", "Error sending timetable change notification", e)
        }
    }
}
