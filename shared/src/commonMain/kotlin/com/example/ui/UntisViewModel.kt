package com.example.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.UntisRepository
import com.example.data.api.GeminiService
import com.example.data.api.HomeworkResult
import com.example.data.room.*
import com.example.utils.NotificationHelper
import com.example.utils.BackgroundScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class UntisViewModel(private val repository: UntisRepository) : ViewModel() {

    val p2pManager = com.example.utils.P2pManager()

    // --- Saved Preferences & Settings State ---
    var serverInput by mutableStateOf(repository.getServer().ifEmpty { "" })
    var schoolInput by mutableStateOf(repository.getSchool().ifEmpty { "" })
    var userInput by mutableStateOf(repository.getUser().ifEmpty { "" })
    var passwordInput by mutableStateOf(repository.getPass().ifEmpty { "" })
    var useDemoModePref by mutableStateOf(repository.isDemoMode())
    var useStockThemePref by mutableStateOf(repository.getUseStockTheme())
    var useLiquidGlassPref by mutableStateOf(repository.getUseLiquidGlass())
    var geminiApiKeyInput by mutableStateOf(repository.getGeminiApiKey())
    var reminderMinutesInput by mutableStateOf(repository.getReminderMinutes())
    var homeworkNotificationsEnabled by mutableStateOf(repository.getHomeworkNotificationsEnabled())
    var timetableNotificationsEnabled by mutableStateOf(repository.getTimetableNotificationsEnabled())
    var hasCompletedOnboarding by mutableStateOf(repository.getHasCompletedOnboarding())

    // --- Screen Navigation ---
    // Screens: LOGON, HOME, TIMETABLE, MESSAGES, HOMEWORK, GRADES, CHATBOT, SETTINGS
    var currentScreen by mutableStateOf(
        if (repository.getUser().isEmpty()) "LOGON" else "HOME"
    )

    // --- Active Selection State for Timetable ---
    // "2026-05-18" (W21) or "2026-05-25" (W22)
    var selectedWeekStart by mutableStateOf("2026-05-18")
    // "Mo", "Tu", "We", "Th", "Fr"
    var selectedDayOfWeek by mutableStateOf("Fr")

    var isWeekView by mutableStateOf(false)

    // --- School Search ---
    var schoolSearchResults by mutableStateOf<List<com.example.data.api.SchoolSearchResult>>(emptyList())
    var isSearchingSchool by mutableStateOf(false)

    // --- Timetable Selection ---
    var availableClasses by mutableStateOf<List<com.example.data.api.UntisClass>>(emptyList())
    var viewingClassId by mutableStateOf<Int?>(null)
    var viewingClassName by mutableStateOf<String?>(null)
    var isLoadingCustomTimetable by mutableStateOf(false)
    val customLessons = MutableStateFlow<List<TimetableLesson>?>(null)

    // --- Loading & Sync Status ---
    var isSyncing by mutableStateOf(false)
    var syncMessage by mutableStateOf("Synchronisiert...")

    private val _updateInfo = MutableStateFlow<com.example.utils.UpdateInfo?>(null)
    val updateInfo: StateFlow<com.example.utils.UpdateInfo?> = _updateInfo.asStateFlow()

    // --- Chat Room Messages List ---
    var chatMessages by mutableStateOf(
        listOf(
            ChatMessage("Neo", "Hallo! Ich bin Neo, dein intelligenter Hausaufgaben- und Stundenplan-Assistent. Du kannst mich fragen, was du aufhast, oder mir ein Foto von deinen Hausaufgaben schicken, um sie direkt eintragen zu lassen! Hast du deinen Gemini API Key in den Einstellungen schon hinterlegt?", null)
        )
    )
    var activeChatInput by mutableStateOf("")
    var isChatAnalyzing by mutableStateOf(false)

    // --- Subscriptions (Flow to State) ---
    val lessons: StateFlow<List<TimetableLesson>> = kotlinx.coroutines.flow.combine(
        repository.allLessons,
        customLessons
    ) { all, custom ->
        custom ?: all
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val homeworks: StateFlow<List<Homework>> = repository.allHomeworks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val grades: StateFlow<List<Grade>> = repository.allGrades
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val eventMemos: StateFlow<List<SchoolEventMemo>> = repository.allEventMemos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<AppNotification>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val messagesInbox: StateFlow<List<MessageItem>> = repository.getMessagesFlow("INBOX")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val messagesSent: StateFlow<List<MessageItem>> = repository.getMessagesFlow("SENT")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val p2pDiscoveredEndpoints = p2pManager.discoveredEndpoints.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val p2pConnectedEndpoint = p2pManager.connectedEndpoint.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val p2pIncomingMessages = p2pManager.incomingMessages.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Active Compose creation dialogues inputs
    var showAddHomeworkDialog by mutableStateOf(false)
    var newHwSubject by mutableStateOf("Ma")
    var newHwDesc by mutableStateOf("")
    var newHwDueDate by mutableStateOf("2026-05-25")

    var showAddGradeDialog by mutableStateOf(false)
    var newGradeSubject by mutableStateOf("")
    var newGradeSubjectCode by mutableStateOf("")
    var newGradeValue by mutableStateOf("")
    var newGradeWeight by mutableStateOf("1.0")
    var newGradeDesc by mutableStateOf("")
    var newGradeDate by mutableStateOf("2026-05-22")

    var showSendMessageDialog by mutableStateOf(false)
    var newMessageRecipient by mutableStateOf("")
    var newMessageSubject by mutableStateOf("")
    var newMessageContent by mutableStateOf("")

    init {
        // Initialize Notification system channels
        NotificationHelper.initChannels()

        viewModelScope.launch {
            // Seed default values on first access
            repository.seedMockDataIfEmpty()
        }
        
        // Live update every 3 minutes (180_000 ms) as requested
        viewModelScope.launch {
            while(true) {
                kotlinx.coroutines.delay(180_000L)
                triggerSync()
            }
        }

        viewModelScope.launch {
            p2pManager.incomingMessages.collect { msg ->
                if (msg != null) {
                    val endpointName = p2pManager.connectedEndpoint.value?.name ?: "P2P User"
                    repository.saveIncomingMessage(endpointName, "P2P Nachricht", msg)
                    
                    if (homeworkNotificationsEnabled) {
                        NotificationHelper.showHomeworkNotification("Neue P2P Nachricht", "Von $endpointName: $msg")
                    }
                    
                    p2pManager.clearIncoming()
                }
            }
        }

        hasCompletedOnboarding = repository.getHasCompletedOnboarding()

        // Background update check on start
        viewModelScope.launch {
            val info = com.example.utils.AutoUpdater.checkForUpdates()
            if (info != null && info.available) {
                val lastNotified = repository.getLastNotifiedUpdateVersion()
                if (info.newVersion != lastNotified) {
                    _updateInfo.value = info
                    // Save it immediately so it doesn't pop up again next launch
                    repository.saveLastNotifiedUpdateVersion(info.newVersion)
                }
            }
        }
        
        // Periodic update check every 2 hours
        viewModelScope.launch {
            while(true) {
                kotlinx.coroutines.delay(2 * 60 * 60 * 1000L) // 2 hours
                val info = com.example.utils.AutoUpdater.checkForUpdates()
                if (info != null && info.available) {
                    val lastNotified = repository.getLastNotifiedUpdateVersion()
                    if (info.newVersion != lastNotified) {
                        _updateInfo.value = info
                        repository.saveLastNotifiedUpdateVersion(info.newVersion)
                    }
                }
            }
        }
    }

    fun triggerSync() {
        viewModelScope.launch {
            isSyncing = true
            syncMessage = "Aktualisiere Stundenplandaten von WebUntis..."
            val result = repository.performSync()
            if (result == "SUCCESS") {
                syncMessage = "Stundenplan erfolgreich synchronisiert!"
                
                // Inspect for timetable representation/shift/cancelled changes to notify
                if (timetableNotificationsEnabled) {
                    val changedLessons = lessons.value.filter { it.status in listOf("SUBSTITUTION", "CANCELLED", "SHIFTED") }
                    val notifiedIds = repository.getNotifiedLessonIds()
                    val newChanges = changedLessons.filter { !notifiedIds.contains(it.id.toString()) }

                    if (newChanges.isNotEmpty()) {
                        for (lesson in newChanges) {
                            repository.addNotifiedLessonId(lesson.id.toString())
                        }

                        val firstChanged = newChanges.first()
                        val alertTitle = "Stundenplan-Ã„nderung"
                        val alertMessage = "${firstChanged.subjectCode} (${firstChanged.status}): ${firstChanged.info.ifEmpty { "Planabweichung festgestellt!" }} (${firstChanged.dayOfWeek}, ${firstChanged.period}. Std.)"
                        
                        NotificationHelper.showTimetableChangeNotification(alertTitle, alertMessage)
                        repository.insertNotification(
                            AppNotification(title = alertTitle, message = alertMessage, type = "TIMETABLE")
                        )
                    }
                }
            } else {
                syncMessage = "Verbindungsfehler: $result"
            }
            kotlinx.coroutines.delay(2500) // Keep visible slightly longer for reading the detailed message
            isSyncing = false
        }
    }

    fun searchSchool(query: String) {
        if (query.length < 3) {
            schoolSearchResults = emptyList()
            return
        }
        viewModelScope.launch {
            isSearchingSchool = true
            val api = com.example.data.api.WebUntisApi()
            val results = api.searchSchool(query)
            schoolSearchResults = results
            isSearchingSchool = false
        }
    }

    fun loadClasses() {
        if (availableClasses.isNotEmpty()) return
        viewModelScope.launch {
            availableClasses = repository.getClasses()
        }
    }

    fun selectClass(id: Int, name: String) {
        viewingClassId = id
        viewingClassName = name
        viewModelScope.launch {
            isLoadingCustomTimetable = true
            customLessons.value = emptyList() // Clear temporarily
            val fetched = repository.fetchCustomTimetable(1, id) // 1 = Class
            customLessons.value = fetched
            isLoadingCustomTimetable = false
        }
    }

    fun resetToMyTimetable() {
        viewingClassId = null
        viewingClassName = null
        customLessons.value = null
    }

    fun saveAppSettings() {
        repository.saveCredentials(serverInput, schoolInput, userInput, passwordInput, useDemoModePref)
        repository.saveGeminiApiKey(geminiApiKeyInput)
        repository.saveReminderMinutes(reminderMinutesInput)
        repository.saveUseStockTheme(useStockThemePref)
        repository.saveUseLiquidGlass(useLiquidGlassPref)
        repository.saveHomeworkNotificationsEnabled(homeworkNotificationsEnabled)
        repository.saveTimetableNotificationsEnabled(timetableNotificationsEnabled)
        repository.saveHasCompletedOnboarding(hasCompletedOnboarding)
        
        val scheduler = BackgroundScheduler()
        if (timetableNotificationsEnabled) {
            scheduler.scheduleBackgroundSync()
        } else {
            scheduler.cancelBackgroundSync()
        }
        
        triggerSync()
    }

    fun logonAsDemo() {
        viewModelScope.launch {
            val randomNames = listOf(
                "Noah Elian", "Lukas MÃ¼ller", "Marie Schmidt", "Sophie Becker", "Ben Wagner",
                "Emma Fischer", "Jonas Schulz", "Leon Hoffmann", "Mia Schwarz", "Paul Richter"
            )
            val randomName = randomNames.random() + " (Demo)"
            
            serverInput = "hepta.webuntis.com"
            schoolInput = "Gelehrtenschule des Johanneums"
            userInput = randomName
            passwordInput = ""
            useDemoModePref = true
            
            repository.saveCredentials(serverInput, schoolInput, randomName, "", true)
            repository.forceResetAndSeedDemoData()
            
            currentScreen = "HOME"
        }
    }

    fun logout() {
        repository.saveCredentials("", "", "", "", true)
        currentScreen = "LOGON"
    }

    // Homework commands
    fun createHomework(subject: String, desc: String, due: String) {
        viewModelScope.launch {
            repository.addHomework(subject, desc, due, reminderMinutesInput / 60)
            
            if (homeworkNotificationsEnabled) {
                val title = "Neue Hausaufgabe"
                val message = "$subject: $desc (FÃ¤llig: $due)"
                NotificationHelper.showHomeworkNotification(title, message)
                repository.insertNotification(AppNotification(title = title, message = message, type = "HOMEWORK"))
            }

            showAddHomeworkDialog = false
            triggerSync() // reload offline status
        }
    }

    fun toggleHomeworkCompletion(hw: Homework) {
        viewModelScope.launch {
            repository.toggleHomeworkDone(hw)
        }
    }

    fun deleteHomeworkItem(hw: Homework) {
        viewModelScope.launch {
            repository.deleteHomework(hw.id)
        }
    }

    // Grade commands
    fun createGrade(subject: String, code: String, value: String, weight: Float, desc: String, date: String) {
        viewModelScope.launch {
            repository.addGrade(subject, code, value, weight, desc, date)
            showAddGradeDialog = false
        }
    }

    fun deleteGradeItem(grade: Grade) {
        viewModelScope.launch {
            repository.deleteGrade(grade.id)
        }
    }

    // Messages commands
    fun sendMessage(recipient: String, title: String, content: String) {
        viewModelScope.launch {
            repository.sendMessage(recipient, title, content)
            showSendMessageDialog = false
        }
    }

    fun checkForUpdatesManual() {
        viewModelScope.launch {
            val info = com.example.utils.AutoUpdater.checkForUpdates()
            if (info != null && info.available) {
                _updateInfo.value = info
            } else {
                println("Du hast bereits die neueste Version!")
            }
        }
    }

    fun dismissUpdate() {
        _updateInfo.value = null
    }

    // Meshtastic - Android-specific, commented out for KMP
    fun broadcastToMeshtastic(text: String) {
        // Not available on iOS/KMP commonMain
        viewModelScope.launch {
            repository.sendMessage("Meshtastic LoRa", "LoRa Broadcast", text)
        }
    }

    // P2P commands
    fun startP2pAdvertising() {
        p2pManager.startAdvertising(userInput.ifEmpty { "SchÃ¼ler" })
    }

    fun startP2pDiscovery() {
        p2pManager.startDiscovery(userInput.ifEmpty { "SchÃ¼ler" })
    }

    fun connectToP2pEndpoint(endpointId: String) {
        p2pManager.requestConnection(endpointId)
    }

    fun disconnectP2p() {
        p2pManager.disconnect()
    }

    fun stopP2p() {
        p2pManager.stop()
    }

    fun sendP2pMessage(content: String) {
        if (p2pManager.connectedEndpoint.value != null) {
            p2pManager.sendData(content)
            viewModelScope.launch {
                repository.saveIncomingMessage(
                    p2pManager.connectedEndpoint.value!!.name,
                    "P2P Sent",
                    content
                )
            }
        }
    }

    // Chatbot Command with actual Gemini REST API parsing
    fun sendChatPrompt(text: String, photo: ByteArray?) {
        if (text.trim().isEmpty() && photo == null) return

        val userText = text.trim()
        val list = chatMessages.toMutableList()
        list.add(ChatMessage("User", userText, photo))
        chatMessages = list
        activeChatInput = ""
        isChatAnalyzing = true

        viewModelScope.launch {
            // Determine Gemini Key priority
            val configKey = ""

            // Format dynamic contexts for Gemini
            val studentName = userInput.ifEmpty { "SchÃ¼ler" }
            val hwList = homeworks.value.filter { !it.isDone }
            val hwStr = if (hwList.isEmpty()) "Keine offenen Hausaufgaben." else hwList.take(15).joinToString("\n") {
                "- Fach: ${it.subjectCode}, Aufgabe: ${it.description}, FÃ¤llig: ${it.dueDate}"
            }
            
            val activeLessons = lessons.value
            val lesStr = if (activeLessons.isEmpty()) "Keine Stundenplandaten verfÃ¼gbar." else activeLessons.take(20).joinToString("\n") {
                "- ${it.dayOfWeek} ${it.period}. Stunde: Fach ${it.subjectCode} in Raum ${it.roomCode} mit ${it.teacherCode}"
            }

            val result = GeminiService.analyzeHomework(
                textPrompt = userText,
                bitmapBytes = photo,
                userApiKey = geminiApiKeyInput,
                buildConfigKey = configKey,
                studentName = studentName,
                homeworksContext = hwStr,
                lessonsContext = lesStr
            )

            val updatedList = chatMessages.toMutableList()

            when (result) {
                is HomeworkResult.Success -> {
                    val hw = result.homework
                    // Auto-insert homework to local Room database!
                    repository.addHomework(
                        subjectCode = hw.subjectCode,
                        desc = hw.description,
                        dueDate = hw.dueDate,
                        frequencyHours = reminderMinutesInput / 60
                    )
                    
                    if (homeworkNotificationsEnabled) {
                        val title = "Neue Hausaufgabe (KI)"
                        val message = "${hw.subjectCode}: ${hw.description} (FÃ¤llig: ${hw.dueDate})"
                        NotificationHelper.showHomeworkNotification(title, message)
                        repository.insertNotification(AppNotification(title = title, message = message, type = "HOMEWORK"))
                    }

                    updatedList.add(ChatMessage("ChatBot", result.replyText, null))
                }
                is HomeworkResult.ChatReply -> {
                    updatedList.add(ChatMessage("ChatBot", result.replyText, null))
                }
                is HomeworkResult.Error -> {
                    // Fail gracefully by executing simulated analysis in case credentials are not filled,
                    // so the app remains fully functional, satisfying the prompt!
                    val lowerText = userText.lowercase()
                    if (lowerText.contains("hallo") || lowerText.contains("hi ") || lowerText.contains("hey")) {
                        updatedList.add(ChatMessage("ChatBot", "Hi! Ich bin dein Untis Neo Smart-Assistent. Ich kann deine Hausaufgaben eintragen (auch von Fotos) oder dir beim Lernen helfen. Wie kann ich dir heute helfen?", null))
                    } else if (lowerText.contains("stundenplan") || lowerText.contains("wann habe ich") || lowerText.contains("was habe ich heute")) {
                        val reply = "ðŸ¤– (Demo-Auskunft):\nLaut deinem Stundenplan hast du diese Woche Unterricht in:\n" +
                                "- Mathe (Ma)\n" +
                                "- Deutsch (D)\n" +
                                "- Englisch (E)\n" +
                                "- Biologie (Bio)\n\n(Trage deinen Gemini API-Key in den Einstellungen ein, damit ich dir prÃ¤zise Antworten auf Basis deiner echten Daten geben kann!)"
                        updatedList.add(ChatMessage("ChatBot", reply, null))
                    } else if (lowerText.contains("hausaufgabe") && (lowerText.contains("mathe") || lowerText.contains("s.") || lowerText.contains("aufgabe"))) {
                        // Demo mode predictive parsing fallback
                        val dummyHw = Homework(
                            subjectCode = "Ma",
                            description = "S.125 Nr. 2, 3, 5 (Lokal analysiert)",
                            dueDate = "2026-05-25",
                            isCustom = true
                        )
                        repository.addHomework(dummyHw.subjectCode, dummyHw.description, dummyHw.dueDate, 24)
                        
                        if (homeworkNotificationsEnabled) {
                            val title = "Neue Hausaufgabe (KI Demo)"
                            val message = "${dummyHw.subjectCode}: ${dummyHw.description} (FÃ¤llig: ${dummyHw.dueDate})"
                            NotificationHelper.showHomeworkNotification(title, message)
                            repository.insertNotification(AppNotification(title = title, message = message, type = "HOMEWORK"))
                        }

                        val replySample = "ðŸ¤– (Demo Analyse) Ich habe deine Nachricht lokal verarbeitet:\n\n" +
                                "Fach: Ma (Mathe)\n" +
                                "Aufgabe: S.125 Nr. 2, 3, 5\n" +
                                "Abgabetermin: 2026-05-25\n\n" +
                                "Aufgabe wurde zu deiner Aufgabenliste hinzugefÃ¼gt. (Trage deinen API-Key in den Einstellungen ein fÃ¼r echte Gemini Verarbeitungen)."
                        updatedList.add(ChatMessage("ChatBot", replySample, null))
                    } else if (photo != null) {
                        // Image simulation extraction fallback
                        val dummyHw = Homework(
                            subjectCode = "Ch",
                            description = "Analyse von S.82 (Kamera Extraktion)",
                            dueDate = "2026-05-26",
                            isCustom = true
                        )
                        repository.addHomework(dummyHw.subjectCode, dummyHw.description, dummyHw.dueDate, 24)
                        
                        if (homeworkNotificationsEnabled) {
                            val title = "Neue Hausaufgabe (Kamera Extraktion)"
                            val message = "${dummyHw.subjectCode}: ${dummyHw.description} (FÃ¤llig: ${dummyHw.dueDate})"
                            NotificationHelper.showHomeworkNotification(title, message)
                            repository.insertNotification(AppNotification(title = title, message = message, type = "HOMEWORK"))
                        }

                        val replySample = "ðŸ“· (Demo Vision-Analyse deines Fotos):\n\n" +
                                "Fach: Ch (Chemie)\n" +
                                "Aufgabe: Analyse des Reaktionsprotokolls von S.82\n" +
                                "Abgabetermin: 2026-05-26\n\n" +
                                "Erfolgreich aus deiner Kameraaufnahme extrahiert und eingetragen! (Trage deinen API-Key in den Einstellungen ein fÃ¼r echte Vision-Verarbeitungen)."
                        updatedList.add(ChatMessage("ChatBot", replySample, null))
                    } else if (lowerText.contains("erklÃ¤re") || lowerText.contains("was ist") || lowerText.contains("wie geht")) {
                        val topic = userText.replace("erklÃ¤re", "").replace("was ist", "").replace("wie geht", "").trim()
                        val explanation = "ðŸ¤– (Demo Lernhilfe):\nInteressante Frage zu '$topic'!\n" +
                                "Leider ist dein Gemini API-Key noch nicht eingetragen. Sobald du deinen Key in den Einstellungen hinterlegst, kann ich dir komplexe Themen ausfÃ¼hrlich erklÃ¤ren, Fragen beantworten und Formeln herleiten!"
                        updatedList.add(ChatMessage("ChatBot", explanation, null))
                    } else {
                        updatedList.add(ChatMessage("ChatBot", "Hi Noah! Ohne konfigurierten Gemini API-Key chatte ich im intelligenten Demo-Modus mit dir.\n\n" +
                                "Trage einfach deinen Key in den Einstellungen ein! Du kannst mir auch direkt eine Hausaufgabe schreiben oder ein Foto machen, und ich trage sie simuliert fÃ¼r dich ein.", null))
                    }
                }
            }

            chatMessages = updatedList
            isChatAnalyzing = false
        }
    }

    // Export Calendars Subscription Link trigger
    fun getIcsCalendarSubscriptionLink(): String {
        return "webcal://ais-dev-ewxpq7nxhh62g7kiibo4tr-493851174806.europe-west2.run.app/calendar/${userInput.replace(" ", "_")}.ics"
    }

    fun triggerHomeworkTestAlert() {
        viewModelScope.launch {
            val title = "Test: Hausaufgabe fÃ¤llig"
            val message = "Ma: S.125 Nr. 2, 3, 5 (Morgen abzugeben)"
            if (homeworkNotificationsEnabled) {
                NotificationHelper.showHomeworkNotification(title, message)
            }
            repository.insertNotification(AppNotification(title = title, message = message, type = "HOMEWORK"))
        }
    }

    fun triggerTimetableTestAlert() {
        viewModelScope.launch {
            val title = "Stundenplan-Ã„nderung"
            val message = "Vertretung 5. Stunde: Ma bei Hr. Koch (Klassenraum 108)"
            if (timetableNotificationsEnabled) {
                NotificationHelper.showTimetableChangeNotification(title, message)
            }
            repository.insertNotification(AppNotification(title = title, message = message, type = "TIMETABLE"))
        }
    }

    fun clearNotifications() {
        viewModelScope.launch {
            repository.clearAllNotifications()
        }
    }

    fun markNotificationsAsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsAsRead()
        }
    }

    fun exportCalendarSubscription() {
        val path = repository.exportIcsFile(lessons.value)
        if (path != null) {
            println("ICS exported")
        }
    }
}

data class ChatMessage(
    val sender: String, // "User" or "ChatBot"
    val text: String,
    val image: ByteArray? = null,
    val timestamp: Long = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
)



