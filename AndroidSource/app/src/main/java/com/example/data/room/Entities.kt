package com.example.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lessons")
data class TimetableLesson(
    @PrimaryKey val id: String, // Combination of day & period & subject to keep Unique
    val dateStr: String, // e.g., "2026-05-22"
    val dayOfWeek: String, // Mo, Tu, We, Th, Fr
    val period: Int, // 1, 2, 3...
    val startTime: String, // e.g. "08:00"
    val endTime: String, // e.g. "08:45"
    val subjectCode: String, // e.g. "Ch", "Bio"
    val subjectName: String, // e.g. "Chemie", "Biologie"
    val teacherCode: String, // e.g. "Fr", "Kg"
    val teacherName: String, // e.g. "Frau Richter", "Herr König"
    val roomCode: String, // e.g. "Che1", "Bio1"
    val colorHex: String, // Custom category color for subjects
    val status: String = "NORMAL", // NORMAL, SUBSTITUTION, CANCELLED, SHIFTED
    val info: String = "", // Info text for cancel/substitution
    val originalRoom: String = ""
)

@Entity(tableName = "homeworks")
data class Homework(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectCode: String,
    val description: String,
    val dueDate: String, // "YYYY-MM-DD"
    val isCustom: Boolean = false, // True if added by user, False if synced from Untis Web API
    val isDone: Boolean = false,
    val reminderFrequencyHours: Int = 24 // 12, 24, 48 hours relative to exam/dueDate
)

@Entity(tableName = "grades")
data class Grade(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectCode: String,
    val subjectName: String,
    val gradeValue: String, // e.g., "1", "2+", "13 Points"
    val weight: Float = 1.0f,
    val description: String,
    val examDate: String
)

@Entity(tableName = "messages")
data class MessageItem(
    @PrimaryKey val id: String,
    val sender: String,
    val recipient: String,
    val subject: String,
    val content: String,
    val timestamp: Long,
    val folder: String // "INBOX", "SENT", "DRAFTS"
)

@Entity(tableName = "schools")
data class SchoolEventMemo(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val type: String // "ANN_BANNER", "INFO"
)

@Entity(tableName = "app_notifications")
data class AppNotification(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val type: String, // "HOMEWORK" or "TIMETABLE"
    val isRead: Boolean = false
)
