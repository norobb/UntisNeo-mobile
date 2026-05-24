package com.example.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.UntisRepository
import com.example.data.room.AppDatabase
import com.example.data.room.AppNotification
import com.example.utils.NotificationHelper
import kotlinx.coroutines.flow.first

class TimetableSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = UntisRepository(applicationContext, database.untisDao())

        // If user hasn't logged in, no point in syncing
        if (repository.getUser().isEmpty() && !repository.isDemoMode()) {
            return Result.success()
        }

        // Only run if notifications are enabled
        if (!repository.getTimetableNotificationsEnabled()) {
            return Result.success()
        }

        try {
            val syncResult = repository.performSync()
            if (syncResult == "SUCCESS") {
                val lessons = repository.allLessons.first()
                val changedLessons = lessons.filter { it.status in listOf("SUBSTITUTION", "CANCELLED", "SHIFTED") }
                val notifiedIds = repository.getNotifiedLessonIds()

                val newChanges = changedLessons.filter { !notifiedIds.contains(it.id.toString()) }

                if (newChanges.isNotEmpty()) {
                    for (lesson in newChanges) {
                        repository.addNotifiedLessonId(lesson.id.toString())
                    }

                    val firstChanged = newChanges.first()
                    val alertTitle = "Stundenplan-Änderung"
                    val alertMessage = "${firstChanged.subjectCode} (${firstChanged.status}): ${firstChanged.info.ifEmpty { "Planabweichung festgestellt!" }} (${firstChanged.dayOfWeek}, ${firstChanged.period}. Std.)"

                    NotificationHelper.sendTimetableChangeNotification(applicationContext, alertTitle, alertMessage)
                    repository.insertNotification(
                        AppNotification(title = alertTitle, message = alertMessage, type = "TIMETABLE")
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("TimetableSyncWorker", "Error during sync", e)
            return Result.retry()
        }

        return Result.success()
    }
}
