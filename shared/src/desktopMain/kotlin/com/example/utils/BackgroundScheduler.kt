package com.example.utils

actual class BackgroundScheduler actual constructor() {
    actual fun scheduleBackgroundSync() {
        println("[Desktop] scheduleBackgroundSync: Scheduled periodic timetable checks.")
    }

    actual fun cancelBackgroundSync() {
        println("[Desktop] cancelBackgroundSync")
    }
}
