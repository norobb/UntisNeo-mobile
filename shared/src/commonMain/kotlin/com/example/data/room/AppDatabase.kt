package com.example.data.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        TimetableLesson::class,
        Homework::class,
        Grade::class,
        MessageItem::class,
        SchoolEventMemo::class,
        AppNotification::class
    ],
    version = 2
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun untisDao(): UntisDao
}
