package com.example.data.room

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UntisDao {
    // Lessons
    @Query("SELECT * FROM lessons ORDER BY dateStr ASC, period ASC")
    fun getAllLessonsFlow(): Flow<List<TimetableLesson>>

    @Query("SELECT * FROM lessons ORDER BY dateStr ASC, period ASC")
    suspend fun getAllLessons(): List<TimetableLesson>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLessons(lessons: List<TimetableLesson>)

    @Query("DELETE FROM lessons")
    suspend fun clearLessons()

    // Homeworks
    @Query("SELECT * FROM homeworks ORDER BY dueDate ASC")
    fun getHomeworksFlow(): Flow<List<Homework>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHomework(hw: Homework)

    @Update
    suspend fun updateHomework(hw: Homework)

    @Query("DELETE FROM homeworks")
    suspend fun clearHomeworks()

    @Query("DELETE FROM homeworks WHERE id = :id")
    suspend fun deleteHomework(id: Long)

    // Grades
    @Query("SELECT * FROM grades ORDER BY examDate DESC")
    fun getGradesFlow(): Flow<List<Grade>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrade(grade: Grade)

    @Query("DELETE FROM grades WHERE id = :id")
    suspend fun deleteGrade(id: Long)

    @Query("DELETE FROM grades")
    suspend fun clearGrades()

    // Messages
    @Query("SELECT * FROM messages WHERE folder = :folder ORDER BY timestamp DESC")
    fun getMessagesFlow(folder: String): Flow<List<MessageItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(msg: MessageItem)

    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun deleteMessage(id: String)

    @Query("DELETE FROM messages")
    suspend fun clearMessages()

    // School Event Memos
    @Query("SELECT * FROM schools")
    fun getEventMemosFlow(): Flow<List<SchoolEventMemo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEventMemo(memo: SchoolEventMemo)

    @Query("DELETE FROM schools")
    suspend fun clearEventMemos()

    // App Notifications
    @Query("SELECT * FROM app_notifications ORDER BY timestamp DESC")
    fun getAllNotificationsFlow(): Flow<List<AppNotification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notif: AppNotification)

    @Query("DELETE FROM app_notifications WHERE id = :id")
    suspend fun deleteNotification(id: Long)

    @Query("DELETE FROM app_notifications")
    suspend fun clearAllNotifications()

    @Query("UPDATE app_notifications SET isRead = 1")
    suspend fun markAllNotificationsAsRead()
}
