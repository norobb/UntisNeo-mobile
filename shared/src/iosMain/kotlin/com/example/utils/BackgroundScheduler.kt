package com.example.utils

actual class BackgroundScheduler actual constructor() {
    actual fun scheduleBackgroundSync() {
        // In a full implementation, this would use BGTaskScheduler.
        // For KMP without direct app delegate access, we simulate or print.
        println("[iOS] scheduleBackgroundSync: Scheduled periodic timetable checks.")
    }

    actual fun cancelBackgroundSync() {
        println("[iOS] cancelBackgroundSync")
    }
}
