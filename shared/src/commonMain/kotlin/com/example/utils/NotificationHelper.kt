package com.example.utils

// KMP Stub - actual notification logic is platform-specific
// On Android, notifications are handled natively via androidMain
object NotificationHelper {
    fun initChannels() {
        // no-op on non-Android platforms
    }

    fun showHomeworkNotification(title: String, content: String) {
        println("[NOTIFICATION] $title: $content")
    }

    fun showTimetableChangeNotification(title: String, content: String) {
        println("[NOTIFICATION] $title: $content")
    }
}
