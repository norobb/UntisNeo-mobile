package com.example.data

import com.example.data.room.*
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class UntisRepository(
    private val settings: Settings,
    private val untisDao: UntisDao
) {

    // Save and load settings
    fun saveCredentials(server: String, school: String, user: String, pass: String, useDemo: Boolean) {
        settings.putString("server", server)
        settings.putString("school", school)
        settings.putString("user", user)
        settings.putString("pass", pass)
        settings.putBoolean("use_demo", useDemo)
    
    }

    fun isDemoMode(): Boolean = settings.getBoolean("use_demo", true)
    fun getServer(): String = settings.getString("server", "")
    fun getSchool(): String = settings.getString("school", "")
    fun getUser(): String = settings.getString("user", "")
    fun getPass(): String = settings.getString("pass", "")

    fun getGeminiApiKey(): String = settings.getString("gemini_api_key", "")
    fun saveGeminiApiKey(key: String) {
        settings.putString("gemini_api_key", key)
    }

    fun getReminderMinutes(): Int = settings.getInt("reminder_minutes", 60)
    fun saveReminderMinutes(minutes: Int) {
        settings.putInt("reminder_minutes", minutes)
    }

    fun getHomeworkNotificationsEnabled(): Boolean = settings.getBoolean("homework_notif_enabled", true)
    fun saveHomeworkNotificationsEnabled(enabled: Boolean) {
        settings.putBoolean("homework_notif_enabled", enabled)
    }

    fun getTimetableNotificationsEnabled(): Boolean = settings.getBoolean("timetable_notif_enabled", true)
    fun saveTimetableNotificationsEnabled(enabled: Boolean) {
        settings.putBoolean("timetable_notif_enabled", enabled)
    }

    fun getUseStockTheme(): Boolean = settings.getBoolean("use_stock_theme", false)
    fun saveUseStockTheme(use: Boolean) {
        settings.putBoolean("use_stock_theme", use)
    }

    fun getUseLiquidGlass(): Boolean = settings.getBoolean("use_liquid_glass", false)
    fun saveUseLiquidGlass(use: Boolean) {
        settings.putBoolean("use_liquid_glass", use)
    }

    fun getSubjectColor(subjectCode: String): String? {
        val color = settings.getString("color_$subjectCode", "")
        return if (color.isNotEmpty()) color else null
    }
    fun saveSubjectColor(subjectCode: String, hexColor: String) {
        settings.putString("color_$subjectCode", hexColor)
    }

    fun getHasCompletedOnboarding(): Boolean = settings.getBoolean("has_completed_onboarding", false)
    fun saveHasCompletedOnboarding(completed: Boolean) {
        settings.putBoolean("has_completed_onboarding", completed)
    }

    fun getLastNotifiedUpdateVersion(): String = settings.getString("last_notified_update_version", "")
    fun saveLastNotifiedUpdateVersion(version: String) {
        settings.putString("last_notified_update_version", version)
    }

    fun getNotifiedLessonIds(): Set<String> = settings.getString("notified_lesson_ids", "").split(",").filter { it.isNotEmpty() }.toSet()
    fun addNotifiedLessonId(id: String) {
        val current = getNotifiedLessonIds().toMutableSet()
        current.add(id)
        settings.putString("notified_lesson_ids", current.joinToString(","))
    }

        // Export ICS Content
    fun generateIcsString(lessons: List<TimetableLesson>): String {
        val sb = StringBuilder()
        sb.append("BEGIN:VCALENDAR\n")
        sb.append("VERSION:2.0\n")
        sb.append("PRODID:-//UntisNeo//Nonsgml//EN\n")
        sb.append("CALSCALE:GREGORIAN\n")
        sb.append("METHOD:PUBLISH\n")

        for (lesson in lessons) {
            try {
                val startParsed = lesson.dateStr.replace("-", "") + "T" + lesson.startTime.replace(":", "") + "00Z"
                val endParsed = lesson.dateStr.replace("-", "") + "T" + lesson.endTime.replace(":", "") + "00Z"
                
                sb.append("BEGIN:VEVENT\n")
                sb.append("UID:${lesson.id}@untisneo.com\n")
                sb.append("DTSTAMP:${startParsed}\n")
                sb.append("DTSTART:${startParsed}\n")
                sb.append("DTEND:${endParsed}\n")
                sb.append("SUMMARY:${lesson.subjectName} (${lesson.subjectCode})\n")
                sb.append("DESCRIPTION:Teacher: ${lesson.teacherName} (${lesson.teacherCode})\\nStatus: ${lesson.status}\\nInfo: ${lesson.info}\n")
                sb.append("LOCATION:${lesson.roomCode}\n")
                sb.append("END:VEVENT\n")
            } catch (e: Exception) {
                // Ignore parse errors for corrupt entries
            }
        }
        sb.append("END:VCALENDAR\n")
        return sb.toString()
    }

    fun exportIcsFile(lessons: List<TimetableLesson>): String = generateIcsString(lessons)

    // Setup Mock and Cached Data
    val allLessons: Flow<List<TimetableLesson>> = untisDao.getAllLessonsFlow()
    val allHomeworks: Flow<List<Homework>> = untisDao.getHomeworksFlow()
    val allGrades: Flow<List<Grade>> = untisDao.getGradesFlow()
    val allEventMemos: Flow<List<SchoolEventMemo>> = untisDao.getEventMemosFlow()
    val allNotifications: Flow<List<AppNotification>> = untisDao.getAllNotificationsFlow()

    fun getMessagesFlow(folder: String): Flow<List<MessageItem>> = untisDao.getMessagesFlow(folder)

    suspend fun insertNotification(notif: AppNotification) {
        untisDao.insertNotification(notif)
    }

    suspend fun deleteNotification(id: Long) {
        untisDao.deleteNotification(id)
    }

    suspend fun clearAllNotifications() {
        untisDao.clearAllNotifications()
    }

    suspend fun markAllNotificationsAsRead() {
        untisDao.markAllNotificationsAsRead()
    }

    suspend fun addHomework(subjectCode: String, desc: String, dueDate: String, frequencyHours: Int) {
        untisDao.insertHomework(
            Homework(
                subjectCode = subjectCode,
                description = desc,
                dueDate = dueDate,
                isCustom = true,
                isDone = false,
                reminderFrequencyHours = frequencyHours
            )
        )
    }

    suspend fun toggleHomeworkDone(hw: Homework) {
        untisDao.updateHomework(hw.copy(isDone = !hw.isDone))
    }

    suspend fun deleteHomework(id: Long) {
        untisDao.deleteHomework(id)
    }

    suspend fun addGrade(subject: String, subjectCode: String, grade: String, weight: Float, desc: String, date: String) {
        untisDao.insertGrade(
            Grade(
                subjectCode = subjectCode,
                subjectName = subject,
                gradeValue = grade,
                weight = weight,
                description = desc,
                examDate = date
            )
        )
    }

    suspend fun deleteGrade(id: Long) {
        untisDao.deleteGrade(id)
    }

    suspend fun sendMessage(recipient: String, subject: String, content: String) {
        val id = kotlin.random.Random.nextLong().toString()
        untisDao.insertMessage(
            MessageItem(
                id = id,
                sender = getUser(),
                recipient = recipient,
                subject = subject,
                content = content,
                timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds(),
                folder = "SENT"
            )
        )
    }

    suspend fun saveIncomingMessage(sender: String, subject: String, content: String) {
        val id = kotlin.random.Random.nextLong().toString()
        untisDao.insertMessage(
            MessageItem(
                id = id,
                sender = sender,
                recipient = getUser(),
                subject = subject,
                content = content,
                timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds(),
                folder = "INBOX"
            )
        )
    }

    suspend fun clearAllData() {
        untisDao.clearLessons()
        untisDao.clearHomeworks()
        untisDao.clearMessages()
        untisDao.clearMemos()
    }
    
    fun exportBackup(): String {
        val backupMap = mutableMapOf<String, String>()
        backupMap["server"] = settings.getString("server", "")
        backupMap["school"] = settings.getString("school", "")
        backupMap["user"] = settings.getString("user", "")
        backupMap["pass"] = settings.getString("pass", "")
        backupMap["gemini_api_key"] = settings.getString("gemini_api_key", "")
        backupMap["reminder_minutes"] = settings.getInt("reminder_minutes", 15).toString()
        backupMap["use_stock_theme"] = settings.getBoolean("use_stock_theme", false).toString()
        backupMap["use_liquid_glass"] = settings.getBoolean("use_liquid_glass", false).toString()
        
        return backupMap.entries.joinToString(separator = ";") { "${it.key}=${it.value}" }
    }
    
    fun importBackup(backupString: String) {
        val pairs = backupString.split(";")
        pairs.forEach { pair ->
            val parts = pair.split("=")
            if (parts.size == 2) {
                val key = parts[0]
                val value = parts[1]
                when(key) {
                    "server" -> settings.putString(key, value)
                    "school" -> settings.putString(key, value)
                    "user" -> settings.putString(key, value)
                    "pass" -> settings.putString(key, value)
                    "gemini_api_key" -> settings.putString(key, value)
                    "reminder_minutes" -> settings.putInt(key, value.toIntOrNull() ?: 15)
                    "use_stock_theme" -> settings.putBoolean(key, value.toBooleanStrictOrNull() ?: false)
                    "use_liquid_glass" -> settings.putBoolean(key, value.toBooleanStrictOrNull() ?: false)
                }
            }
        }
    }

    suspend fun forceResetAndSeedDemoData() {
        untisDao.clearLessons()
        untisDao.clearHomeworks()
        untisDao.clearGrades()
        untisDao.clearMessages()
        untisDao.clearEventMemos()
        seedMockDataIfEmpty()
    }

    // Trigger full Seed of dummy data matching school "Gelehrtenschule des Johanneums"
    suspend fun seedMockDataIfEmpty() {
        val existing = untisDao.getAllLessons()
        if (existing.isNotEmpty()) return

        println("UntisRepository: Seeding mock data for Gelehrtenschule...")

        // Seed 1: Lessons for W 21 (18 May to 22 May 2026)
        val lessonsW21 = listOf(
            // Mon 18 May
            TimetableLesson("l01", "2026-05-18", "Mo", 1, "08:00", "08:45", "E", "Englisch", "Rg", "Frau Richter", "108", "4A9DFF"),
            TimetableLesson("l02", "2026-05-18", "Mo", 2, "08:50", "09:35", "E", "Englisch", "Rg", "Frau Richter", "108", "4A9DFF"),
            TimetableLesson("l03", "2026-05-18", "Mo", 3, "09:55", "10:40", "Geo", "Geografie", "Se", "Herr Seemann", "108", "CAD982", "NORMAL", "Hausaufgaben bis morgen!"),
            TimetableLesson("l04", "2026-05-18", "Mo", 4, "10:45", "11:30", "Ma", "Mathe", "Er", "Herr Erichsen", "108", "D56BFF", "SHIFTED", "Verschoben in den Nachmittag"),
            TimetableLesson("l05", "2026-05-18", "Mo", 5, "11:35", "12:20", "Gri", "Griechisch", "Ri", "Frau Ritter", "108", "FF6384"),
            TimetableLesson("l06", "2026-05-18", "Mo", 7, "13:15", "14:00", "Phy", "Physik", "Ko", "Herr Koch", "Phy2", "36A2EB"),
            TimetableLesson("l07", "2026-05-18", "Mo", 8, "14:05", "14:50", "Phy", "Physik", "Ko", "Herr Koch", "Phy2", "36A2EB"),

            // Tue 19 May
            TimetableLesson("l08", "2026-05-19", "Tu", 1, "08:00", "08:45", "Ch", "Chemie", "Fr", "Frau Frank", "Che1", "4BC0C0"),
            TimetableLesson("l09", "2026-05-19", "Tu", 2, "08:50", "09:35", "Ch", "Chemie", "Fr", "Frau Frank", "Che1", "4BC0C0"),
            TimetableLesson("l10", "2026-05-19", "Tu", 3, "09:55", "10:40", "Sp", "Sport", "Rd", "Herr Radtke", "TH", "9966FF"),
            TimetableLesson("l11", "2026-05-19", "Tu", 4, "10:45", "11:30", "Sp", "Sport", "Rd", "Herr Radtke", "TH", "9966FF"),
            TimetableLesson("l12", "2026-05-19", "Tu", 5, "11:35", "12:20", "Rat", "Klassenrat", "Rg", "Frau Richter", "108", "FF9F40"),
            TimetableLesson("l13", "2026-05-19", "Tu", 7, "13:15", "14:00", "Mu", "Musik", "Wi", "Frau Wild", "Mu2", "FF6384"),
            TimetableLesson("l14", "2026-05-19", "Tu", 8, "14:05", "14:50", "Mu", "Musik", "Wi", "Frau Wild", "Mu2", "FF6384"),

            // Wed 20 May
            TimetableLesson("l15", "2026-05-20", "We", 1, "08:00", "08:45", "F&L", "Freies Lernen", "Rg", "Frau Richter", "108", "C9CBCF"),
            TimetableLesson("l16", "2026-05-20", "We", 2, "08:50", "09:35", "F&L", "Freies Lernen", "Rg", "Frau Richter", "108", "C9CBCF"),
            TimetableLesson("l17", "2026-05-20", "We", 3, "09:55", "10:40", "E", "Englisch", "Rg", "Frau Richter", "108", "4A9DFF"),
            TimetableLesson("l18", "2026-05-20", "We", 4, "10:45", "11:30", "Mu", "Musik", "Wi", "Frau Wild", "Mu1", "FF6384"),
            TimetableLesson("l19", "2026-05-20", "We", 5, "11:35", "12:20", "Ma", "Mathe", "Er", "Herr Erichsen", "108", "D56BFF"),

            // Thu 21 May
            TimetableLesson("l20", "2026-05-21", "Th", 1, "08:00", "08:45", "Bio", "Biologie", "Kg", "Herr König", "Bio1", "22C55E"),
            TimetableLesson("l21", "2026-05-21", "Th", 2, "08:50", "09:35", "Bio", "Biologie", "Kg", "Herr König", "Bio1", "22C55E"),
            TimetableLesson("l22", "2026-05-21", "Th", 3, "09:55", "10:40", "Ma", "Mathe", "Er", "Herr Erichsen", "108", "D56BFF"),
            TimetableLesson("l23", "2026-05-21", "Th", 5, "11:35", "12:20", "Ma", "Mathe", "Er", "Herr Erichsen", "108", "D56BFF", "SUBSTITUTION", "Vertretung durch Hr. Koch"),
            TimetableLesson("l24", "2026-05-21", "Th", 7, "13:15", "14:00", "D", "Deutsch", "Fl", "Frau Flemming", "108", "EF4444"),

            // Fri 22 May (Highlighted in primary app screenshots)
            TimetableLesson("l25", "2026-05-22", "Fr", 1, "08:00", "08:45", "Gri", "Griechisch", "Ri", "Frau Ritter", "108", "FF6384"),
            TimetableLesson("l26", "2026-05-22", "Fr", 2, "08:50", "09:35", "D", "Deutsch", "Fl", "Frau Flemming", "108", "EF4444"),
            TimetableLesson("l27", "2026-05-22", "Fr", 3, "09:55", "10:40", "D", "Deutsch", "Fl", "Frau Flemming", "108", "EF4444", "SUBSTITUTION", "Raumänderung: Turnhalle entfällt"),
            TimetableLesson("l28", "2026-05-22", "Fr", 4, "10:45", "11:30", "Ge", "Geschichte", "Bt", "Herr Bathke", "108", "EAB308"),
            TimetableLesson("l29", "2026-05-22", "Fr", 5, "11:35", "12:20", "L", "Latein", "Kn", "Frau Knospe", "108", "14B8A6"),
            TimetableLesson("l30", "2026-05-22", "Fr", 7, "13:15", "14:00", "L", "Latein", "Kn", "Frau Knospe", "108", "14B8A6"),
            TimetableLesson("l31", "2026-05-22", "Fr", 9, "14:45", "15:30", "Orch", "Orchester", "Sk", "Herr Spengler", "Aula", "8B5CF6")
        )

        val lessonsW22 = listOf(
            // Mon 25 May -> Pentecost Holiday
            TimetableLesson("h1", "2026-05-25", "Mo", 1, "08:00", "08:45", "[P]", "Holiday P", "", "", "[P]", "5C6BC0", "CANCELLED", "Feiertag - Pfingstmontag"),
            TimetableLesson("h2", "2026-05-25", "Mo", 2, "08:50", "09:35", "[P]", "Holiday P", "", "", "[P]", "5C6BC0", "CANCELLED", "Feiertag - Pfingstmontag"),
            TimetableLesson("h3", "2026-05-25", "Mo", 3, "09:55", "10:40", "[P]", "Holiday P", "", "", "[P]", "5C6BC0", "CANCELLED", "Feiertag - Pfingstmontag"),
            TimetableLesson("h4", "2026-05-25", "Mo", 4, "10:45", "11:30", "[P]", "Holiday P", "", "", "[P]", "5C6BC0", "CANCELLED", "Feiertag - Pfingstmontag"),
            TimetableLesson("h5", "2026-05-25", "Mo", 5, "11:35", "12:20", "[P]", "Holiday P", "", "", "[P]", "5C6BC0", "CANCELLED", "Feiertag - Pfingstmontag"),
            TimetableLesson("h6", "2026-05-25", "Mo", 7, "13:15", "14:00", "[P]", "Holiday P", "", "", "[P]", "5C6BC0", "CANCELLED", "Feiertag - Pfingstmontag"),

            // Tue 26 May
            TimetableLesson("l32", "2026-05-26", "Tu", 1, "08:00", "08:45", "Ch", "Chemie", "Fr", "Frau Frank", "Che1", "4BC0C0"),
            TimetableLesson("l33", "2026-05-26", "Tu", 2, "08:50", "09:35", "Ch", "Chemie", "Fr", "Frau Frank", "Che1", "4BC0C0"),
            TimetableLesson("l34", "2026-05-26", "Tu", 3, "09:55", "10:40", "Sp", "Sport", "Rd", "Herr Radtke", "Are", "9966FF"),
            TimetableLesson("l35", "2026-05-26", "Tu", 4, "10:45", "11:30", "Ch", "Chemie", "Fr", "Frau Frank", "Bio2", "22C55E", "SUBSTITUTION", "Fachvertretung im Biologie-Übungsraum"),
            TimetableLesson("l36", "2026-05-26", "Tu", 5, "11:35", "12:20", "Bio", "Biologie", "Kg", "Herr König", "Bio2", "22C55E", "SUBSTITUTION", "Kurzfristige Raumänderung"),

            // Wed 27 May
            TimetableLesson("l37", "2026-05-27", "We", 1, "08:00", "08:45", "F&L", "Freies Lernen", "Rg", "Frau Richter", "108", "C9CBCF"),
            TimetableLesson("l38", "2026-05-27", "We", 2, "08:50", "09:35", "F&L", "Freies Lernen", "Rg", "Frau Richter", "108", "C9CBCF"),
            TimetableLesson("l39", "2026-05-27", "We", 3, "09:55", "10:40", "E", "Englisch", "Rg", "Frau Richter", "108", "4A9DFF"),
            TimetableLesson("l40", "2026-05-27", "We", 5, "11:35", "12:20", "Ma", "Mathe", "Er", "Herr Erichsen", "108", "D56BFF"),

            // Thu 28 May
            TimetableLesson("l41", "2026-05-28", "Th", 1, "08:00", "08:45", "Bio", "Biologie", "Kg", "Herr König", "Bio1", "22C55E"),
            TimetableLesson("l42", "2026-05-28", "Th", 2, "08:50", "09:35", "Bio", "Biologie", "Kg", "Herr König", "Bio1", "22C55E"),
            TimetableLesson("l43", "2026-05-28", "Th", 3, "09:55", "10:40", "Ma", "Mathe", "Er", "Herr Erichsen", "108", "D56BFF"),
            TimetableLesson("l44", "2026-05-28", "Th", 5, "11:35", "12:20", "L", "Latein", "Kn", "Frau Knospe", "108", "14B8A6"),

            // Fri 29 May
            TimetableLesson("l45", "2026-05-29", "Fr", 1, "08:00", "08:45", "Gri", "Griechisch", "Ri", "Frau Ritter", "108", "FF6384"),
            TimetableLesson("l46", "2026-05-29", "Fr", 2, "08:50", "09:35", "Gri", "Griechisch", "Ri", "Frau Ritter", "108", "FF6384"),
            TimetableLesson("l47", "2026-05-29", "Fr", 3, "09:55", "10:40", "D", "Deutsch", "Fl", "Frau Flemming", "108", "EF4444"),
            TimetableLesson("l48", "2026-05-29", "Fr", 4, "10:45", "11:30", "Ge", "Geschichte", "Bt", "Herr Bathke", "108", "EAB308"),
            TimetableLesson("l49", "2026-05-29", "Fr", 5, "11:35", "12:20", "L", "Latein", "Kn", "Frau Knospe", "108", "14B8A6"),
            TimetableLesson("l50", "2026-05-29", "Fr", 9, "14:45", "15:30", "Orch", "Orchester", "Sk", "Herr Spengler", "Aula", "8B5CF6", "SUBSTITUTION", "Zusatzprobe in der Aula")
        )

        untisDao.insertLessons(lessonsW21)
        untisDao.insertLessons(lessonsW22)

        // Seed 2: Homework
        untisDao.insertHomework(Homework(1, "Ma", "Übungsaufgaben S. 124 Nr. 1-4, 7abc", "2026-05-25", false, false, 24))
        untisDao.insertHomework(Homework(2, "D", "Lektüre Faust I - Fausts Monolog analysieren", "2026-05-27", false, true, 12))
        untisDao.insertHomework(Homework(3, "Ch", "Protokoll zum Neutralisationsexperiment fertigstellen", "2026-05-26", true, false, 48))

        // Seed 3: Grades
        untisDao.insertGrade(Grade(1, "Mathe", "Ma", "1+", 1.0f, "Klausur Integration & Vektoren", "2026-05-15"))
        untisDao.insertGrade(Grade(2, "Deutsch", "D", "2+", 1.0f, "Aufsatz Literaturepoche Klassik", "2026-05-12"))
        untisDao.insertGrade(Grade(3, "Physik", "Phy", "12 Punkte", 0.5f, "Test Elektromagnetismus", "2026-05-09"))

        // Seed 4: SchoolEventMemos
        untisDao.insertEventMemo(SchoolEventMemo("memo1", "Reinigungsdienste", "Reinigungsdienste in dieser Woche: Höfe: 7b und 8b Umgebung: 9b und 10a Reinigungskräfte unterstützen im Hauptgebäude ab 13:30.", "ANN_BANNER"))

        // Seed 5: Welcome Message
        val userName = getUser().ifEmpty { "Neo Nutzer" }
        untisDao.insertMessage(
            MessageItem(
                id = "m1",
                sender = "Untis Neo System",
                recipient = userName,
                subject = "Willkommen bei Untis Neo!",
                content = "Hallo $userName,\n\n" +
                          "willkommen in deiner neuen Untis Neo App!\n\n" +
                          "Hier ist ein kleines Tutorial für deinen Start:\n" +
                          "1. Stundenplan: Navigiere zum Kalender-Icon, um deine Fächer, Lehrer und Räume übersichtlich im Nothing-Look zu sehen.\n" +
                          "2. Hausaufgaben: Füge Aufgaben hinzu und behalte den Überblick, was zu tun ist.\n" +
                          "3. Notenspiegel: Trage deine Noten ein, die App berechnet automatisch den Schnitt.\n" +
                          "4. Nachrichten: Die Messaging-Funktion funktioniert nun exklusiv zwischen Untis Neo Clients.\n\n" +
                          "Viel Spaß beim Organisieren deines Schulalltags!",
                timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds() - 86400000,
                folder = "INBOX"
            )
        )
    }

    // Call WebUntis JSON-RPC Direct Endpoint
    suspend fun performSync(): String {
        if (isDemoMode()) {
            seedMockDataIfEmpty()
            return "SUCCESS"
        }

        return try {
            val serverIp = getServer()
            val schoolId = getSchool()
            val username = getUser()
            val password = getPass()

            if (serverIp.isEmpty() || schoolId.isEmpty() || username.isEmpty() || password.isEmpty()) {
                println("UntisRepository: Credentials missing.")
                return "Zugangsdaten unvollständig."
            }

            println("UntisRepository: Syncing with WebUntis API at $serverIp for school $schoolId...")
            
            val api = com.example.data.api.WebUntisApi()
            val lessons = api.fetchTimetable(serverIp, schoolId, username, password)
            
            if (lessons != null) {
                // Apply custom subject colors if configured
                val coloredLessons = lessons.map { lesson ->
                    val customColor = getSubjectColor(lesson.subjectCode)
                    if (customColor != null) lesson.copy(colorHex = customColor) else lesson
                }

                untisDao.clearLessons()
                untisDao.insertLessons(coloredLessons)
                println("UntisRepository: Successfully fetched and saved ${coloredLessons.size} lessons.")
                "SUCCESS"
            } else {
                "Leere Stundenplandaten erhalten"
            }
        } catch (e: Exception) {
            val errMsg = e.message ?: "Unbekannter Fehler bei der Synchronisation"
            println("UntisRepository: API sync failed: $errMsg. Exception: $e")
            errMsg
        }
    }

    suspend fun getClasses(): List<com.example.data.api.UntisClass> {
        if (isDemoMode()) return emptyList()
        return try {
            val api = com.example.data.api.WebUntisApi()
            api.fetchClasses(getServer(), getSchool(), getUser(), getPass())
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun fetchCustomTimetable(customType: Int, customId: Int): List<TimetableLesson> {
        if (isDemoMode()) return emptyList()
        return try {
            val api = com.example.data.api.WebUntisApi()
            val fetched = api.fetchTimetable(getServer(), getSchool(), getUser(), getPass(), customType, customId) ?: emptyList()
            fetched.map { lesson ->
                val customColor = getSubjectColor(lesson.subjectCode)
                if (customColor != null) lesson.copy(colorHex = customColor) else lesson
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}



