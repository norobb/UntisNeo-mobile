package com.example.utils

actual class BackgroundScheduler actual constructor() {
    actual fun scheduleBackgroundSync() {
        // In a real Android app, we would enqueue a PeriodicWorkRequest with WorkManager here.
        println("[Android] scheduleBackgroundSync: Scheduled periodic timetable checks.")
    }

    actual fun cancelBackgroundSync() {
        println("[Android] cancelBackgroundSync")
    }
}
