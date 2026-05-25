package com.example.utils

expect class NotificationPlatformHelper() {
    fun initChannels()
    fun showNotification(title: String, content: String)
}

object NotificationHelper {
    private val platformHelper = NotificationPlatformHelper()

    fun initChannels() {
        platformHelper.initChannels()
    }

    fun showHomeworkNotification(title: String, content: String) {
        platformHelper.showNotification(title, content)
    }

    fun showTimetableChangeNotification(title: String, content: String) {
        platformHelper.showNotification(title, content)
    }
}
