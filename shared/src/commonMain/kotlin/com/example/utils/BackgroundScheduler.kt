package com.example.utils

expect class BackgroundScheduler() {
    fun scheduleBackgroundSync()
    fun cancelBackgroundSync()
}
