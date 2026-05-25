package com.example.data.room

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

/**
 * In-memory stub implementation of UntisDao for platforms without Room support (iOS).
 * All data is stored in-memory and lost when the app closes.
 */
class InMemoryUntisDao : UntisDao {
    private val _lessons = MutableStateFlow<List<TimetableLesson>>(emptyList())
    private val _homeworks = MutableStateFlow<List<Homework>>(emptyList())
    private val _grades = MutableStateFlow<List<Grade>>(emptyList())
    private val _messages = MutableStateFlow<List<MessageItem>>(emptyList())
    private val _memos = MutableStateFlow<List<SchoolEventMemo>>(emptyList())
    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())

    override fun getAllLessonsFlow(): Flow<List<TimetableLesson>> = _lessons.asStateFlow()
    override suspend fun getAllLessons(): List<TimetableLesson> = _lessons.value

    override suspend fun insertLessons(lessons: List<TimetableLesson>) {
        val existing = _lessons.value.toMutableList()
        lessons.forEach { new ->
            val idx = existing.indexOfFirst { it.id == new.id }
            if (idx >= 0) existing[idx] = new else existing.add(new)
        }
        _lessons.value = existing.sortedWith(compareBy({ it.dateStr }, { it.period }))
    }

    override suspend fun clearLessons() { _lessons.value = emptyList() }

    override fun getHomeworksFlow(): Flow<List<Homework>> = _homeworks.asStateFlow()

    override suspend fun insertHomework(hw: Homework) {
        val list = _homeworks.value.toMutableList()
        val idx = list.indexOfFirst { it.id == hw.id }
        if (idx >= 0) list[idx] = hw else list.add(hw)
        _homeworks.value = list.sortedBy { it.dueDate }
    }

    override suspend fun updateHomework(hw: Homework) = insertHomework(hw)

    override suspend fun clearHomeworks() { _homeworks.value = emptyList() }

    override suspend fun deleteHomework(id: Long) {
        _homeworks.value = _homeworks.value.filter { it.id != id }
    }

    override fun getGradesFlow(): Flow<List<Grade>> = _grades.asStateFlow()

    override suspend fun insertGrade(grade: Grade) {
        val list = _grades.value.toMutableList()
        val idx = list.indexOfFirst { it.id == grade.id }
        if (idx >= 0) list[idx] = grade else list.add(grade)
        _grades.value = list.sortedByDescending { it.examDate }
    }

    override suspend fun deleteGrade(id: Long) {
        _grades.value = _grades.value.filter { it.id != id }
    }

    override suspend fun clearGrades() { _grades.value = emptyList() }

    override fun getMessagesFlow(folder: String): Flow<List<MessageItem>> {
        return _messages.map { list -> list.filter { it.folder == folder } }
    }

    override suspend fun insertMessage(msg: MessageItem) {
        val list = _messages.value.toMutableList()
        list.add(msg)
        _messages.value = list
    }

    override suspend fun deleteMessage(id: String) {
        _messages.value = _messages.value.filter { it.id != id }
    }

    override suspend fun clearMessages() { _messages.value = emptyList() }

    override fun getEventMemosFlow(): Flow<List<SchoolEventMemo>> = _memos.asStateFlow()

    override suspend fun insertEventMemo(memo: SchoolEventMemo) {
        val list = _memos.value.toMutableList()
        val idx = list.indexOfFirst { it.id == memo.id }
        if (idx >= 0) list[idx] = memo else list.add(memo)
        _memos.value = list
    }

    override suspend fun clearEventMemos() { _memos.value = emptyList() }

    override fun getAllNotificationsFlow(): Flow<List<AppNotification>> = _notifications.asStateFlow()

    override suspend fun insertNotification(notif: AppNotification) {
        val list = _notifications.value.toMutableList()
        list.add(notif)
        _notifications.value = list
    }

    override suspend fun deleteNotification(id: Long) {
        _notifications.value = _notifications.value.filter { it.id != id }
    }

    override suspend fun clearAllNotifications() { _notifications.value = emptyList() }

    override suspend fun markAllNotificationsAsRead() {
        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
    }
}
