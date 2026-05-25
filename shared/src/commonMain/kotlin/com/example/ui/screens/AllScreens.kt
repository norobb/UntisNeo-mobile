package com.example.ui.screens
import com.example.ui.StringResources


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.room.*
import com.example.ui.ChatMessage
import com.example.ui.UntisViewModel
import com.example.ui.components.*
import kotlinx.datetime.*

// --- LOGON SCREEN ---
@Composable
fun LogonScreen(viewModel: UntisViewModel) {
    var step by remember { mutableStateOf(1) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NothingBlack)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier
                .widthIn(max = 450.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Ndot Header logo
            NothingHeader(text = StringResources.get("Untis Neo"), fontSize = 32.sp, showRedDot = true)
            Text(
                text = StringResources.get("Die schönere Oberfläche für deinen Schulalltag."),
                
                color = NothingMutedGray,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                color = NothingCardGray,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, if (androidx.compose.foundation.isSystemInDarkTheme()) Color(0xFF222222) else Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = StringResources.get("ANMELDUNG"),
                        fontWeight = FontWeight.Bold,
                        color = NothingWhite,
                        fontSize = 16.sp
                    )

                    NothingTextField(
                        value = viewModel.serverInput,
                        onValueChange = { viewModel.serverInput = it },
                        label = "Server"
                    )

                    var searchExpanded by remember { mutableStateOf(false) }

                    Box(modifier = Modifier.fillMaxWidth()) {
                        NothingTextField(
                            value = viewModel.schoolInput,
                            onValueChange = { 
                                viewModel.schoolInput = it
                                viewModel.searchSchool(it)
                                searchExpanded = it.length >= 3 
                            },
                            label = "Schule (Suchen oder eingeben)"
                        )
                        DropdownMenu(
                            expanded = searchExpanded && viewModel.schoolSearchResults.isNotEmpty(),
                            onDismissRequest = { searchExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.9f).background(NothingCardGray)
                        ) {
                            viewModel.schoolSearchResults.forEach { school ->
                                DropdownMenuItem(
                                    text = { 
                                        Column {
                                            Text(school.displayName, fontWeight = FontWeight.Bold, color = NothingWhite)
                                            Text(school.address, fontSize = 12.sp, color = NothingMutedGray)
                                        }
                                    },
                                    onClick = {
                                        viewModel.schoolInput = school.loginName
                                        viewModel.serverInput = school.serverUrl
                                        searchExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    NothingTextField(
                        value = viewModel.userInput,
                        onValueChange = { viewModel.userInput = it },
                        label = "Benutzername"
                    )

                    NothingTextField(
                        value = viewModel.passwordInput,
                        onValueChange = { viewModel.passwordInput = it },
                        label = "Passwort",
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Password)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.useDemoModePref = !viewModel.useDemoModePref },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Checkbox(
                            checked = viewModel.useDemoModePref,
                            onCheckedChange = { viewModel.useDemoModePref = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = NothingRed, // Red active check state!
                                uncheckedColor = Color(0xFF333333),
                                checkmarkColor = MaterialTheme.colorScheme.background
                            )
                        )
                        Column {
                            Text(StringResources.get("Demo-Modus aktivieren"),
                                color = NothingWhite,
                                
                                fontSize = 13.sp
                            )
                            Text(StringResources.get("Lädt die echten Screenshots-Daten zur Vorschau!"),
                                color = NothingMutedGray,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            NothingButton(
                text = "Verbinden & Starten",
                onClick = {
                    if (viewModel.userInput.isEmpty() && !viewModel.useDemoModePref) {
                        println(StringResources.get("Bitte gib einen Benutzernamen ein oder starte den Demo-Modus!"))
                    } else {
                        viewModel.saveAppSettings()
                        viewModel.currentScreen = "HOME"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            NothingButton(
                text = "Demo-Modus starten",
                onClick = {
                    viewModel.logonAsDemo()
                },
                modifier = Modifier.fillMaxWidth(),
                isPrimary = false
            )
        }
    }
}


// --- HOME DASHBOARD SCREEN ---
@Composable
fun HomeScreen(viewModel: UntisViewModel) {
    val lessons by viewModel.lessons.collectAsState()
    val homeworks by viewModel.homeworks.collectAsState()
    val memos by viewModel.eventMemos.collectAsState()

    // Current Date Formatter (KMP-compatible)
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val curDateStr = "${now.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }}, ${now.dayOfMonth.toString().padStart(2, '0')}. ${now.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${now.year}"

    // Active substitution alarm count
    val substitutionsTodayCount = lessons.filter {
        it.dateStr == "2026-05-22" && (it.status == "SUBSTITUTION" || it.status == "SHIFTED")
    }.size

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NothingBlack)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Header Status bar
        item {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text(StringResources.get("LERNPLATZ"), fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif) },
                actions = {
                    IconButton(onClick = { viewModel.triggerSync() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Sync")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }

        // Top syncing status
        if (viewModel.isSyncing) {
            item {
                Surface(
                    color = NothingCardGray,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (androidx.compose.foundation.isSystemInDarkTheme()) Color(0xFF222222) else Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = NothingWhite, strokeWidth = 2.dp)
                        Text(viewModel.syncMessage, fontFamily = FontFamily.Monospace, color = NothingWhite, fontSize = 12.sp)
                    }
                }
            }
        }

        // Real-Time Substituted/Room Change Alert system
        if (substitutionsTodayCount > 0) {
            item {
                Surface(
                    color = Color(0x20FF3131), // matches new NothingRed
                    shape = RoundedCornerShape(28.dp), // modern 28.dp rounded shape
                    border = BorderStroke(1.dp, NothingRed),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(NothingRed, shape = CircleShape)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "VERTRETUNGSALARM",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = NothingRed,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Du hast heute $substitutionsTodayCount kurzfristige Vertretungen oder Raumänderungen! Prüfe deinen Stundenplan.",
                                fontFamily = FontFamily.Monospace,
                                color = NothingWhite,
                                fontSize = 12.sp
                            )
                        }
                        Button(
                            onClick = {
                                viewModel.selectedWeekStart = "2026-05-18"
                                viewModel.selectedDayOfWeek = "Fr"
                                viewModel.currentScreen = "TIMETABLE"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NothingRed, contentColor = NothingBlack), // high contrast black on red button!
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text("Öffnen", fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Seeding Banner Announcement (mimicking Gelehrtenschule desc Johanneums 5886)
        if (memos.isNotEmpty()) {
            items(memos) { memo ->
                Surface(
                    color = Color(0xFFF59E0B).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Notifications, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = memo.title.uppercase(),
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFF59E0B),
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = memo.content,
                            fontFamily = FontFamily.Monospace,
                            color = NothingWhite,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // Quick Stats/Shortcuts Widget (Pixel/Nothing Cards layout)
        item {
            val isDark = androidx.compose.foundation.isSystemInDarkTheme()
            val borderColor = if (isDark) Color(0xFF222222) else Color(0xFFE2E8F0)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Widget Card 1: Homeworks Todo
                Surface(
                    color = NothingCardGray,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, borderColor),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.currentScreen = StringResources.get("HOMEWORK") }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val activeHwList = homeworks.filter { !it.isDone }
                        Text(
                            text = "${activeHwList.size}",
                            fontSize = 32.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.ExtraBold,
                            color = NothingWhite
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = StringResources.get("Aufgaben offen"),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold,
                            color = NothingMutedGray
                        )
                    }
                }

                // Widget Card 2: Messages / Absences combo
                Surface(
                    color = NothingCardGray,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, borderColor),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.currentScreen = "MESSAGES" }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "0",
                            fontSize = 32.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.ExtraBold,
                            color = NothingWhite
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = StringResources.get("Fehlstunden (0 krank)"),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold,
                            color = NothingMutedGray
                        )
                    }
                }
            }
        }

        // School Information Quick shortcuts mimicking original Untis
        item {
            val isDark = androidx.compose.foundation.isSystemInDarkTheme()
            val borderColor = if (isDark) Color(0xFF222222) else Color(0xFFE2E8F0)

            Surface(
                color = NothingCardGray,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, borderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { /* Sprechstunden */ }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = NothingWhite, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(StringResources.get("Lehrer Sprechstunden"), fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, color = NothingWhite, fontSize = 13.sp)
                        }
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = NothingMutedGray, modifier = Modifier.size(20.dp))
                    }
                    Divider(color = borderColor)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.selectedWeekStart = "2026-05-25"
                                viewModel.selectedDayOfWeek = "Mo"
                                viewModel.currentScreen = "TIMETABLE"
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DateRange, contentDescription = null, tint = NothingWhite, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(StringResources.get("Ferien & Feiertage"), fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, color = NothingWhite, fontSize = 13.sp)
                        }
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = NothingMutedGray, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }

        // Prompt user for smart assist chatbot shortcuts
        item {
            val isDark = androidx.compose.foundation.isSystemInDarkTheme()
            val borderColor = if (isDark) Color(0xFF222222) else Color(0xFFE2E8F0)

            Surface(
                color = NothingCardGray,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, borderColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.currentScreen = "CHATBOT" }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(NothingRed.copy(alpha = 0.15f), shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Face, contentDescription = null, tint = NothingRed)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(StringResources.get("AI HAUSAUFGABEN HELFER"),
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp,
                            color = NothingWhite,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(StringResources.get("Fotografiere dein Buch oder Arbeitsblatt, um Hausaufgaben direkt einzutragen!"),
                            fontFamily = FontFamily.SansSerif,
                            color = NothingMutedGray,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }
}


// --- TIMETABLE SCREEN ---
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun TimetableScreen(viewModel: UntisViewModel) {
    val lessons by viewModel.lessons.collectAsState()
    
    // Determine unique weeks using KMP string parsing (dateStr is "yyyy-MM-dd")
    fun getMonday(dateStr: String): String {
        // Parse yyyy-MM-dd and calculate Monday of that week
        return try {
            val ld = LocalDate.parse(dateStr)
            val dow = ld.dayOfWeek.ordinal // 0=Mon in kotlinx.datetime
            val monday = ld.minus(DatePeriod(days = dow))
            monday.toString()
        } catch (e: Exception) { dateStr }
    }
    val weeks = remember(lessons) {
        lessons.map { lesson -> getMonday(lesson.dateStr) }.distinct().sorted()
    }
    
    val pagerState = androidx.compose.foundation.pager.rememberPagerState(
        initialPage = 0,
        pageCount = { weeks.size.coerceAtLeast(1) }
    )

    androidx.compose.runtime.LaunchedEffect(weeks, viewModel.selectedWeekStart) {
        if (weeks.isNotEmpty()) {
            val index = weeks.indexOf(viewModel.selectedWeekStart)
            if (index in weeks.indices && pagerState.currentPage != index) {
                pagerState.scrollToPage(index)
            }
        }
    }

    var selectedDayIndex by remember { mutableStateOf(0) } // 0=Mo, 1=Tu, 2=We, 3=Th, 4=Fr
    val days = listOf("Mo" to "Montag", "Tu" to "Dienstag", "We" to "Mittwoch", "Th" to "Donnerstag", "Fr" to "Freitag")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NothingBlack)
    ) {
        // Timetable Header
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("Stundenplan", fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif) },
                actions = {
                    IconButton(onClick = { viewModel.isWeekView = !viewModel.isWeekView }) {
                        Icon(if (viewModel.isWeekView) Icons.Default.DateRange else Icons.Outlined.DateRange, contentDescription = "Ansicht wechseln")
                    }
                    IconButton(onClick = { viewModel.triggerSync() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Sync")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )

            // Pager Indicator / Week Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val currentWeekStr = weeks.getOrNull(pagerState.currentPage) ?: "Woche"
                val endDateStr = try {
                    val monday = LocalDate.parse(currentWeekStr)
                    val friday = monday.plus(DatePeriod(days = 4))
                    friday.toString()
                } catch(e: Exception) { currentWeekStr }

                val displayTitle = if (currentWeekStr.length >= 8 && endDateStr.length >= 8) {
                    "${currentWeekStr.substring(8)}.${currentWeekStr.substring(5,7)}. - ${endDateStr.substring(8)}.${endDateStr.substring(5,7)}."
                } else currentWeekStr

                Text(
                    text = displayTitle,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = NothingMutedGray,
                    modifier = Modifier.padding(start = 4.dp)
                )
                
                // Indicators
                if (weeks.size > 1) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (i in weeks.indices) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        color = if (i == pagerState.currentPage) NothingWhite else NothingCardGray,
                                        shape = CircleShape
                                    )
                            )
                        }
                    }
                }
            }
            
            // Day Selector (Only in Day View)
            if (!viewModel.isWeekView) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    days.forEachIndexed { index, day ->
                        val isSelected = selectedDayIndex == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp)
                                .height(40.dp)
                                .background(
                                    color = if (isSelected) NothingWhite else NothingCardGray,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedDayIndex = index },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day.first,
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) NothingBlack else NothingMutedGray,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        } // Header End

        // Pager for Weeks View
        if (weeks.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(StringResources.get("Keine Stunden."), fontFamily = FontFamily.SansSerif, color = NothingMutedGray)
            }
        } else {
            androidx.compose.foundation.pager.HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                val currentWeekStart = weeks.getOrNull(page) ?: return@HorizontalPager
                if (viewModel.isWeekView) {
                    val weekLessons = lessons.filter { getMonday(it.dateStr) == currentWeekStart }
                    if (weekLessons.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Frei!", fontFamily = FontFamily.SansSerif, color = NothingMutedGray, fontSize = 18.sp)
                        }
                    } else {
                        val maxPeriod = weekLessons.maxOfOrNull { it.period } ?: 10
                        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp)) {
                            // Header row with days
                            Row(modifier = Modifier.fillMaxWidth().padding(start = 32.dp)) {
                                days.forEach { day ->
                                    Text(
                                        text = day.first,
                                        modifier = Modifier.weight(1f).padding(4.dp),
                                        textAlign = TextAlign.Center,
                                        color = NothingMutedGray,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(maxPeriod) { periodIndex ->
                                    val period = periodIndex + 1
                                    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min).padding(vertical = 2.dp)) {
                                        Text(
                                            text = "$period.",
                                            modifier = Modifier.width(32.dp).padding(top = 12.dp),
                                            textAlign = TextAlign.Center,
                                            color = NothingMutedGray,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        days.forEach { day ->
                                            val lesson = weekLessons.find { it.dayOfWeek == day.first && it.period == period }
                                            Box(
                                                modifier = Modifier.weight(1f).fillMaxHeight().padding(2.dp),
                                                contentAlignment = Alignment.TopCenter
                                            ) {
                                                if (lesson != null) {
                                                    val isCancelled = lesson.status == "CANCELLED"
                                                    val isSubst = lesson.status == "SUBSTITUTION"
                                                    val colorFromHex = try {
                                                        val hexLong = lesson.colorHex.toLongOrNull(16) ?: 0xAAAAAA
                                                        Color((hexLong or 0xFF000000L).toInt())
                                                    } catch (e: Exception) { NothingMutedGray }
                                                    
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .background(
                                                                if (isCancelled) Color(0x20FF0000) else colorFromHex.copy(alpha = 0.15f),
                                                                RoundedCornerShape(8.dp)
                                                            )
                                                            .border(
                                                                1.dp,
                                                                if (isCancelled) Color(0x50FF0000) else colorFromHex.copy(alpha = 0.3f),
                                                                RoundedCornerShape(8.dp)
                                                            )
                                                            .padding(vertical = 8.dp, horizontal = 2.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                            Text(
                                                                text = lesson.subjectCode,
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = if (isCancelled) NothingMutedGray else NothingWhite,
                                                                textDecoration = if (isCancelled) TextDecoration.LineThrough else null,
                                                                maxLines = 1, overflow = TextOverflow.Ellipsis
                                                            )
                                                            Text(
                                                                text = lesson.roomCode,
                                                                fontSize = 9.sp,
                                                                color = if (isSubst) Color(0xFFFFA500) else NothingMutedGray,
                                                                maxLines = 1, overflow = TextOverflow.Ellipsis
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    val filteredLessons = lessons.filter {
                        getMonday(it.dateStr) == currentWeekStart && it.dayOfWeek == days[selectedDayIndex].first
                    }.sortedBy { it.period }
                    
                    if (filteredLessons.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Frei!", fontFamily = FontFamily.SansSerif, color = NothingMutedGray, fontSize = 18.sp)
                        }
                    } else {
                        androidx.compose.foundation.lazy.LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(filteredLessons) { lesson ->
                                val colorFromHex = try {
                                    val hexLong = lesson.colorHex.toLongOrNull(16) ?: 0xAAAAAA
                                    Color((hexLong or 0xFF000000L).toInt())
                                } catch (e: Exception) {
                                    NothingMutedGray
                                }
                                val isCancelled = lesson.status == "CANCELLED"
                                val isSubstituted = lesson.status == "SUBSTITUTION"
                                
                                // UntisPlus Liquid Glass Card Design
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        // Blur effect is not fully supported on all KMP targets natively without modifiers,
                                        // so we use a translucent background to simulate "Liquid Glass"
                                        .background(
                                            color = if (isCancelled) Color(0x20FF0000) else colorFromHex.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(32.dp)
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (isCancelled) Color(0x50FF0000) else colorFromHex.copy(alpha = 0.3f),
                                            shape = RoundedCornerShape(32.dp)
                                        )
                                        .padding(24.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = lesson.subjectCode,
                                                fontFamily = FontFamily.SansSerif,
                                                fontSize = 24.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isCancelled) NothingMutedGray else NothingWhite,
                                                textDecoration = if (isCancelled) TextDecoration.LineThrough else null
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = NothingMutedGray, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = lesson.roomCode,
                                                    fontFamily = FontFamily.SansSerif,
                                                    fontSize = 14.sp,
                                                    color = if (isSubstituted) Color(0xFFFFA500) else NothingMutedGray,
                                                    fontWeight = if (isSubstituted) FontWeight.Bold else FontWeight.Normal
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Icon(Icons.Default.Person, contentDescription = null, tint = NothingMutedGray, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = lesson.teacherCode,
                                                    fontFamily = FontFamily.SansSerif,
                                                    fontSize = 14.sp,
                                                    color = if (isSubstituted) Color(0xFFFFA500) else NothingMutedGray,
                                                    fontWeight = if (isSubstituted) FontWeight.Bold else FontWeight.Normal
                                                )
                                            }
                                        }
                                        
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "${lesson.period}. Stunde",
                                                fontFamily = FontFamily.SansSerif,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = NothingWhite
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = if (isCancelled) "Entfällt" else if (isSubstituted) "Vertretung" else "Regulär",
                                                fontFamily = FontFamily.SansSerif,
                                                fontSize = 12.sp,
                                                color = if (isCancelled) NothingRed else if (isSubstituted) Color(0xFFFFA500) else colorFromHex
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun getGermanDayName(code: String): String {
    return when(code) {
        "Mo" -> "Montag"
        "Tu" -> "Dienstag"
        "We" -> "Mittwoch"
        "Th" -> "Donnerstag"
        "Fr" -> "Freitag"
        else -> code
    }
}


// --- MESSAGES SCREEN ---
@Composable
fun MessagesScreen(viewModel: UntisViewModel) {
    var selectedTab by remember { mutableStateOf("INBOX") }
    var searchInput by remember { mutableStateOf("") }

    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val borderColor = if (isDark) Color(0xFF222222) else Color(0xFFE2E8F0)

    val inbox by viewModel.messagesInbox.collectAsState()
    val sent by viewModel.messagesSent.collectAsState()
    val alerts by viewModel.notifications.collectAsState()

    val currentList = if (selectedTab == "INBOX") inbox else sent
    val filteredList = currentList.filter {
        it.sender.lowercase().contains(searchInput.lowercase()) ||
                it.subject.lowercase().contains(searchInput.lowercase()) ||
                it.content.lowercase().contains(searchInput.lowercase())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NothingBlack)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NothingHeader(text = StringResources.get("Mitteilungen"), fontSize = 28.sp)

            // Tabs toggle: Inbox, Sent, Alerts
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "INBOX" to "Eingang",
                    "SENT" to "Gesendet",
                    "ALERTS" to "Alerts",
                    "P2P" to "Radar"
                ).forEach { tabInfo ->
                    Button(
                        onClick = { selectedTab = tabInfo.first },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedTab == tabInfo.first) NothingWhite else NothingCardGray,
                            contentColor = if (selectedTab == tabInfo.first) NothingBlack else NothingWhite
                        ),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = tabInfo.second,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            if (selectedTab == "ALERTS") {
                // Actions banner for Notification Center
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "BENACHRICHTIGUNGEN VERLAUF",
                        color = NothingMutedGray,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp
                    )
                    if (alerts.isNotEmpty()) {
                        Text(
                            text = "ALLE LÖSCHEN",
                            color = NothingRed,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier
                                .clickable { viewModel.clearNotifications() }
                                .padding(4.dp)
                        )
                    }
                }
            } else {
                // Search input field matching Untis screenshot
                NothingTextField(
                    value = searchInput,
                    onValueChange = { searchInput = it },
                    label = StringResources.get("Inhalt oder Person suchen"),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = NothingMutedGray) }
                )
            }
        }

        // List Scroll
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (selectedTab == "P2P") {
                item {
                    val p2pConnected by viewModel.p2pConnectedEndpoint.collectAsState()
                    val p2pDiscovered by viewModel.p2pDiscoveredEndpoints.collectAsState()
                    var msgText by remember { mutableStateOf("") }
                    
                    Surface(color = NothingCardGray, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("OFFLINE RADAR", color = NothingRed, fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif)
                            Text("Verbinde dich direkt mit Mitschülern über Bluetooth & lokales WLAN, ganz ohne Internetverbindung.", color = NothingMutedGray, fontSize = 12.sp, lineHeight = 16.sp)
                            
                            if (p2pConnected == null) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    NothingButton("Suchen", onClick = { viewModel.startP2pDiscovery() }, modifier = Modifier.weight(1f))
                                    NothingButton("Sichtbar", onClick = { viewModel.startP2pAdvertising() }, modifier = Modifier.weight(1f), isPrimary = false)
                                }
                            } else {
                                Text("Verbunden mit: ${p2pConnected!!.name}", color = NothingWhite, fontWeight = FontWeight.Bold)
                                NothingTextField(value = msgText, onValueChange = { msgText = it }, label = "Nachricht eingeben")
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    NothingButton("Senden", onClick = { viewModel.sendP2pMessage(msgText); msgText = "" }, modifier = Modifier.weight(1f))
                                    NothingButton("Trennen", onClick = { viewModel.disconnectP2p() }, modifier = Modifier.weight(1f), isPrimary = false)
                                }
                            }
                        }
                    }
                    
                    if (p2pConnected == null && p2pDiscovered.isNotEmpty()) {
                        Text("Gefundene Mitschüler:", color = NothingWhite, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                            p2pDiscovered.forEach { endpoint ->
                                Surface(color = NothingCardGray, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().clickable { viewModel.connectToP2pEndpoint(endpoint.id) }) {
                                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(endpoint.name, color = NothingWhite, fontFamily = FontFamily.SansSerif)
                                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = NothingMutedGray)
                                    }
                                }
                            }
                        }
                    }
                    
                    Surface(color = NothingCardGray, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("LORA MESHTASTIC", color = NothingRed, fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif)
                            Text("Sende Nachrichten weitreichend über dein verbundenes Heltec LoRa Modul an andere UntisNeo Nutzer.", color = NothingMutedGray, fontSize = 12.sp, lineHeight = 16.sp)
                            
                            var loraText by remember { mutableStateOf("") }
                            NothingTextField(value = loraText, onValueChange = { loraText = it }, label = "LoRa Broadcast")
                            NothingButton("Über Meshtastic Senden", onClick = { viewModel.broadcastToMeshtastic(loraText); loraText = "" }, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            } else if (selectedTab == "ALERTS") {
                if (alerts.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = null, tint = NothingMutedGray, modifier = Modifier.size(48.dp))
                            Text(
                                text = "Keine Benachrichtigungen vorhanden.",
                                fontFamily = FontFamily.SansSerif,
                                color = NothingMutedGray,
                                fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    items(alerts) { alert ->
                        Surface(
                            color = NothingCardGray,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, borderColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(if (alert.type == "HOMEWORK") NothingRed.copy(alpha = 0.15f) else NothingWhite.copy(alpha = 0.1f), shape = CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (alert.type == "HOMEWORK") Icons.Default.Edit else Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = if (alert.type == "HOMEWORK") NothingRed else NothingWhite,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = alert.title,
                                        fontFamily = FontFamily.SansSerif,
                                        fontWeight = FontWeight.Bold,
                                        color = NothingWhite,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = alert.message,
                                        fontFamily = FontFamily.SansSerif,
                                        color = NothingMutedGray,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    // Format timestamp as readable string (KMP-compatible)
                                    val alertDate = Instant.fromEpochMilliseconds(alert.timestamp)
                                        .toLocalDateTime(TimeZone.currentSystemDefault())
                                    val alertDateStr = "${alertDate.dayOfMonth.toString().padStart(2,'0')}. ${alertDate.monthNumber.toString().padStart(2,'0')} ${alertDate.hour.toString().padStart(2,'0')}:${alertDate.minute.toString().padStart(2,'0')}"
                                    Text(
                                        text = alertDateStr,
                                        fontFamily = FontFamily.Monospace,
                                        color = NothingMutedGray.copy(alpha = 0.6f),
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                if (filteredList.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.Email, contentDescription = null, tint = NothingMutedGray, modifier = Modifier.size(48.dp))
                        Text(
                            text = StringResources.get("Keine Nachrichten vorhanden."),
                            fontFamily = FontFamily.Monospace,
                            color = NothingMutedGray,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                items(filteredList) { msg ->
                    var isExpanded by remember { mutableStateOf(false) }

                    Surface(
                        color = NothingCardGray,
                        shape = RoundedCornerShape(24.dp), // modern 24.dp round
                        border = BorderStroke(1.dp, Color(0xFF333333)), // updated border color
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isExpanded = !isExpanded }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(NothingWhite.copy(alpha = 0.1f), shape = CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = msg.sender.firstOrNull()?.toString()?.uppercase() ?: "S",
                                            color = NothingWhite,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = msg.sender,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            color = NothingWhite,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "Betreff: ${msg.subject}",
                                            fontFamily = FontFamily.Monospace,
                                            color = NothingMutedGray,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                // Format timestamp (KMP-compatible)
                                val msgDate = Instant.fromEpochMilliseconds(msg.timestamp)
                                    .toLocalDateTime(TimeZone.currentSystemDefault())
                                val msgDateStr = "${msgDate.dayOfMonth.toString().padStart(2,'0')}.${msgDate.monthNumber.toString().padStart(2,'0')} ${msgDate.hour.toString().padStart(2,'0')}:${msgDate.minute.toString().padStart(2,'0')}"
                                Text(
                                    text = msgDateStr,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = NothingMutedGray
                                )
                            }

                            AnimatedVisibility(visible = isExpanded) {
                                Column(modifier = Modifier.padding(top = 12.dp)) {
                                    Divider(color = Color(0xFF2C2C2E))
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = msg.content,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp,
                                        color = NothingWhite,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

        // Compose Message Floating button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            FloatingActionButton(
                onClick = { viewModel.showSendMessageDialog = true },
                containerColor = NothingWhite,
                contentColor = NothingBlack,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = StringResources.get("Nachricht schreiben"))
            }
        }
    }

    // Modal Compose Message form dialog
    if (viewModel.showSendMessageDialog) {
        Dialog(onDismissRequest = { viewModel.showSendMessageDialog = false }) {
            Surface(
                color = NothingCardGray,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color(0xFF2C2C2E)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(StringResources.get("NEUE NACHRICHT"),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = NothingWhite,
                        fontSize = 16.sp
                    )

                    NothingTextField(
                        value = viewModel.newMessageRecipient,
                        onValueChange = { viewModel.newMessageRecipient = it },
                        label = "Empfänger (nur Untis Neo Nutzer)"
                    )

                    NothingTextField(
                        value = viewModel.newMessageSubject,
                        onValueChange = { viewModel.newMessageSubject = it },
                        label = "Betreff"
                    )

                    NothingTextField(
                        value = viewModel.newMessageContent,
                        onValueChange = { viewModel.newMessageContent = it },
                        label = "Inhalt der Nachricht (Mehrzeilig)",
                        singleLine = false
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.showSendMessageDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = NothingWhite),
                            border = BorderStroke(1.dp, Color(0xFF2C2C2E)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(StringResources.get("Bestätigen"), fontFamily = FontFamily.Monospace)
                        }

                        Button(
                            onClick = {
                                viewModel.sendMessage(
                                    viewModel.newMessageRecipient,
                                    viewModel.newMessageSubject,
                                    viewModel.newMessageContent
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NothingWhite, contentColor = NothingBlack),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(StringResources.get("Senden"), fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }
    }
}


// --- HOMEWORKS SCREEN ---
@Composable
fun HomeworkScreen(viewModel: UntisViewModel) {
    val homeworks by viewModel.homeworks.collectAsState()
    var showOnlyTodo by remember { mutableStateOf(false) }

    val filteredHw = homeworks.filter { if (showOnlyTodo) !it.isDone else true }
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val borderColor = if (isDark) Color(0xFF222222) else Color(0xFFE2E8F0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NothingBlack)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NothingHeader(text = StringResources.get("Hausaufgaben"), fontSize = 28.sp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(StringResources.get("Nur ungelöste Aufgaben"),
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Medium,
                    color = NothingWhite,
                    fontSize = 14.sp
                )
                Switch(
                    checked = showOnlyTodo,
                    onCheckedChange = { showOnlyTodo = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = NothingWhite,
                        checkedTrackColor = NothingRed,
                        uncheckedThumbColor = NothingMutedGray,
                        uncheckedTrackColor = NothingDarkGray
                    )
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (filteredHw.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(StringResources.get("Sehr schön, keine Hausaufgaben ausstehend!"),
                            fontFamily = FontFamily.SansSerif,
                            color = NothingMutedGray,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(filteredHw) { hw ->
                    Surface(
                        color = NothingCardGray,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, borderColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Checkbox(
                                checked = hw.isDone,
                                onCheckedChange = { viewModel.toggleHomeworkCompletion(hw) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = NothingRed,
                                    uncheckedColor = if (isDark) Color(0xFF333333) else Color(0xFFCCCCCC),
                                    checkmarkColor = NothingBlack
                                )
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .background(NothingWhite.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = hw.subjectCode,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = NothingWhite
                                        )
                                    }
                                    if (hw.isCustom) {
                                        Box(
                                            modifier = Modifier
                                                .background(NothingRed.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = StringResources.get("PRIVAT"),
                                                fontFamily = FontFamily.SansSerif,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = NothingRed
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = hw.description,
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 14.sp,
                                    color = if (hw.isDone) NothingMutedGray else NothingWhite,
                                    textDecoration = if (hw.isDone) TextDecoration.LineThrough else null
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Fällig bis: ${hw.dueDate}",
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 12.sp,
                                    color = NothingMutedGray
                                )
                            }

                            IconButton(onClick = { viewModel.deleteHomeworkItem(hw) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Entfernen", tint = NothingMutedGray)
                            }
                        }
                    }
                }
            }
        }

        // Custom manual add Homework button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            FloatingActionButton(
                onClick = { viewModel.showAddHomeworkDialog = true },
                containerColor = NothingWhite,
                contentColor = NothingBlack,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Eintrag hinzufügen")
            }
        }
    }

    if (viewModel.showAddHomeworkDialog) {
        Dialog(onDismissRequest = { viewModel.showAddHomeworkDialog = false }) {
            Surface(
                color = NothingCardGray,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, borderColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(StringResources.get("EIGENE HAUSAUFGABE"),
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        color = NothingWhite,
                        fontSize = 16.sp
                    )

                    NothingTextField(
                        value = viewModel.newHwSubject,
                        onValueChange = { viewModel.newHwSubject = it },
                        label = "Fach Code (z.B. Ma, D, Ch)"
                    )

                    NothingTextField(
                        value = viewModel.newHwDesc,
                        onValueChange = { viewModel.newHwDesc = it },
                        label = "Aufgabenbeschreibung"
                    )

                    NothingTextField(
                        value = viewModel.newHwDueDate,
                        onValueChange = { viewModel.newHwDueDate = it },
                        label = "Fälligkeitsdatum (YYYY-MM-DD)"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.showAddHomeworkDialog = false },
                            border = BorderStroke(1.dp, NothingWhite.copy(alpha = 0.25f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NothingWhite),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.weight(1f).height(44.dp)
                        ) {
                            Text(StringResources.get("Abbrechen"), fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Button(
                            onClick = {
                                viewModel.createHomework(
                                    viewModel.newHwSubject,
                                    viewModel.newHwDesc,
                                    viewModel.newHwDueDate
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NothingWhite, contentColor = NothingBlack),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.weight(1f).height(44.dp)
                        ) {
                            Text(StringResources.get("Hinzufügen"), fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}


// --- GRADES TRACKER SCREEN ---
@Composable
fun GradesScreen(viewModel: UntisViewModel) {
    val grades by viewModel.grades.collectAsState()

    // Advanced Average calculator
    var selectedGradeScalePoints by remember { mutableStateOf(false) } // false = 1 to 6 scale, true = 0 to 15 scale

    val avgGradeStr = remember(grades, selectedGradeScalePoints) {
        if (grades.isEmpty()) "--" else {
            var sum = 0f
            var weightSum = 0f
            for (g in grades) {
                // Parse grade values like "1", "2+", "13", "12 Points"
                val cleaned = g.gradeValue.replace("+", ".75").replace("-", ".25").replace(" Punkte", "").replace(" Points", "").trim()
                val parsed = cleaned.toFloatOrNull()
                if (parsed != null) {
                    sum += parsed * g.weight
                    weightSum += g.weight
                }
            }
            if (weightSum > 0) {
                val avg = sum / weightSum
                val rounded = (avg * 100).toInt() / 100.0
                "${rounded}"
            } else "--"
        }
    }

    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val borderColor = if (isDark) Color(0xFF222222) else Color(0xFFE2E8F0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NothingBlack)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NothingHeader(text = StringResources.get("Notenspiegel"), fontSize = 28.sp)

            // Dynamic Average Stats widget card
            Surface(
                color = NothingCardGray,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, borderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = StringResources.get("NOTENSCHNITT"),
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold,
                            color = NothingMutedGray,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Ø $avgGradeStr",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold,
                            color = NothingWhite,
                            fontSize = 32.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = StringResources.get("Unter Berücksichtigung aller Gewichtungen"),
                            fontFamily = FontFamily.SansSerif,
                            color = NothingMutedGray,
                            fontSize = 11.sp
                        )
                    }

                    // Theme toggle point scale
                    Button(
                        onClick = { selectedGradeScalePoints = !selectedGradeScalePoints },
                        colors = ButtonDefaults.buttonColors(containerColor = NothingWhite, contentColor = NothingBlack),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (selectedGradeScalePoints) StringResources.get("0-15 PUNKTE") else StringResources.get("1-6 SCHULNOTEN"),
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (grades.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(StringResources.get("Noch keine Noten eingetragen."),
                            fontFamily = FontFamily.SansSerif,
                            color = NothingMutedGray,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                items(grades) { g ->
                    Surface(
                        color = NothingCardGray,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, borderColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .background(NothingWhite.copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = g.subjectCode,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 10.sp,
                                            color = NothingWhite,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(
                                        text = g.subjectName,
                                        fontFamily = FontFamily.SansSerif,
                                        fontWeight = FontWeight.Bold,
                                        color = NothingWhite,
                                        fontSize = 15.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = g.description, fontFamily = FontFamily.SansSerif, color = NothingMutedGray, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = "Gewicht: ${g.weight} | Datum: ${g.examDate}", fontFamily = FontFamily.SansSerif, color = NothingMutedGray, fontSize = 11.sp)
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(NothingWhite, shape = CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = g.gradeValue,
                                        color = NothingBlack,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 16.sp
                                    )
                                }

                                IconButton(onClick = { viewModel.deleteGradeItem(g) }) {
                                    Icon(Icons.Default.Delete, contentDescription = StringResources.get("Löschen"), tint = NothingMutedGray)
                                }
                            }
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            FloatingActionButton(
                onClick = { viewModel.showAddGradeDialog = true },
                containerColor = NothingWhite,
                contentColor = NothingBlack,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Eintrag hinzufügen")
            }
        }
    }

    if (viewModel.showAddGradeDialog) {
        Dialog(onDismissRequest = { viewModel.showAddGradeDialog = false }) {
            Surface(
                color = NothingCardGray,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, borderColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(StringResources.get("NEUE NOTE EINTRAGEN"),
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        color = NothingWhite,
                        fontSize = 16.sp
                    )

                    NothingTextField(
                        value = viewModel.newGradeSubject,
                        onValueChange = { viewModel.newGradeSubject = it },
                        label = "Fachname (z.B. Mathematik)"
                    )

                    NothingTextField(
                        value = viewModel.newGradeSubjectCode,
                        onValueChange = { viewModel.newGradeSubjectCode = it },
                        label = "Fach Code (z.B. Ma)"
                    )

                    NothingTextField(
                        value = viewModel.newGradeValue,
                        onValueChange = { viewModel.newGradeValue = it },
                        label = "Notenwert (z.B. 1+, 12, 2.5)"
                    )

                    NothingTextField(
                        value = viewModel.newGradeWeight,
                        onValueChange = { viewModel.newGradeWeight = it },
                        label = "Gewichtung (z.B. 1.0, 0.5)"
                    )

                    NothingTextField(
                        value = viewModel.newGradeDesc,
                        onValueChange = { viewModel.newGradeDesc = it },
                        label = "Leistungsnachweis (z.B. Klausur)"
                    )

                    NothingTextField(
                        value = viewModel.newGradeDate,
                        onValueChange = { viewModel.newGradeDate = it },
                        label = "Prüfungsdatum (YYYY-MM-DD)"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.showAddGradeDialog = false },
                            border = BorderStroke(1.dp, NothingWhite.copy(alpha = 0.25f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NothingWhite),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.weight(1f).height(44.dp)
                        ) {
                            Text(StringResources.get("Bestätigen"), fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Button(
                            onClick = {
                                val wVal = viewModel.newGradeWeight.toFloatOrNull() ?: 1.0f
                                viewModel.createGrade(
                                    viewModel.newGradeSubject,
                                    viewModel.newGradeSubjectCode,
                                    viewModel.newGradeValue,
                                    wVal,
                                    viewModel.newGradeDesc,
                                    viewModel.newGradeDate
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NothingWhite, contentColor = NothingBlack),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.weight(1f).height(44.dp)
                        ) {
                            Text(StringResources.get("Eintragen"), fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}


// --- AI CHATBOT SCREEN ---
@Composable
fun ChatbotScreen(viewModel: UntisViewModel) {
    var selectedImageState by remember { mutableStateOf<ByteArray?>(null) }
    var showApiSettingsDialog by remember { mutableStateOf(false) }
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val borderColor = if (isDark) Color(0xFF222222) else Color(0xFFE2E8F0)
    val uriHandler = LocalUriHandler.current

    // Camera/gallery not available in KMP commonMain - placeholder only

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NothingBlack)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NothingHeader(text = StringResources.get("Neo Smart-Assistant"), fontSize = 28.sp)
                IconButton(onClick = { showApiSettingsDialog = true }) {
                    Icon(Icons.Default.Settings, contentDescription = "API Settings", tint = NothingMutedGray)
                }
            }
            Text(
                text = StringResources.get("Stelle Fragen zum Unterricht, plane deinen Schultag oder fotografiere deine Hausaufgabe!"),
                fontFamily = FontFamily.SansSerif,
                color = NothingMutedGray,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }

        if (showApiSettingsDialog) {
            Dialog(onDismissRequest = { showApiSettingsDialog = false }) {
                Surface(
                    color = NothingCardGray,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, borderColor),
                    modifier = Modifier.fillMaxWidth().padding(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Gemini API Konfiguration",
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold,
                            color = NothingWhite,
                            fontSize = 18.sp
                        )
                        
                        Text(
                            text = "Hole dir deinen kostenlosen API Key im Google AI Studio.",
                            fontFamily = FontFamily.SansSerif,
                            color = NothingMutedGray,
                            fontSize = 13.sp
                        )

                        NothingButton(
                            text = "Link öffnen",
                            onClick = {
                                uriHandler.openUri("https://aistudio.google.com/app/apikey")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            isPrimary = false
                        )
                        
                        NothingTextField(
                            value = viewModel.geminiApiKeyInput,
                            onValueChange = { viewModel.geminiApiKeyInput = it },
                            label = "Dein API Key"
                        )
                        
                        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                            NothingButton(
                                text = "Speichern & Schließen",
                                onClick = {
                                    viewModel.saveAppSettings()
                                    showApiSettingsDialog = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Chat logs bubble scroll list
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(viewModel.chatMessages) { chat ->
                val isMe = chat.sender == "User"

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                ) {
                    Column(
                        modifier = Modifier.widthIn(max = 290.dp),
                        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                    ) {
                        // Sender ID
                        Text(
                            text = chat.sender.uppercase(),
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold,
                            color = if (isMe) NothingWhite else NothingRed,
                            fontSize = 9.sp,
                            modifier = Modifier.padding(bottom = 2.dp),
                            letterSpacing = 1.sp
                        )

                        Surface(
                            color = if (isMe) NothingCardGray else (if (isDark) Color(0xFF151515) else Color(0xFFF1F5F9)),
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isMe) 16.dp else 4.dp,
                                bottomEnd = if (isMe) 4.dp else 16.dp
                            ),
                            border = BorderStroke(1.dp, borderColor)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                if (chat.image != null) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    // Image preview not available in KMP commonMain
                                    // (ByteArray -> ImageBitmap conversion is platform-specific)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(80.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFF1A1A1A))
                                            .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "📷 Bild angehängt",
                                            color = NothingMutedGray,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 12.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                }
                                Text(
                                    text = chat.text,
                                    fontFamily = FontFamily.SansSerif,
                                    color = NothingWhite,
                                    fontSize = 13.sp,
                                    lineHeight = 19.sp
                                )
                            }
                        }
                    }
                }
            }

            if (viewModel.isChatAnalyzing) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Surface(
                            color = NothingCardGray,
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, borderColor)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(12.dp), color = NothingWhite, strokeWidth = 2.dp)
                                Text(StringResources.get("Analysiere mit Gemini AI..."), fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 12.sp, color = NothingWhite)
                            }
                        }
                    }
                }
            }
        }

        // Selected Image pre-send preview
        selectedImageState?.let { img ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .background(NothingCardGray, RoundedCornerShape(12.dp))
                    .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(12.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1A1A1A)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("📷", fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = StringResources.get("Foto angehängt und bereit zum Absenden!"),
                    color = NothingWhite,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { selectedImageState = null }) {
                    Icon(Icons.Default.Close, contentDescription = "Löschen", tint = NothingRed)
                }
            }
        }

        // Input entry bar
        Surface(
            color = NothingCardGray,
            border = BorderStroke(1.dp, borderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Interactive helper shortcuts triggers
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Quick Action: S.124
                    Button(
                        onClick = {
                            viewModel.activeChatInput = "Mathe Hausaufgabe S.124 Nr. 1-4 bis Montag eintragen"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Color(0xFF1E1E1E) else Color(0xFFE2E8F0)),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text("📝 Mathe S.124 eintragen", fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = NothingWhite)
                    }

                    // Quick Action: Homework Overview
                    Button(
                        onClick = {
                            viewModel.activeChatInput = "Welche Hausaufgaben habe ich aktuell auf?"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Color(0xFF1E1E1E) else Color(0xFFE2E8F0)),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text("📋 Hausaufgaben abfragen", fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = NothingWhite)
                    }

                    // Quick Action: Timetable Overview
                    Button(
                        onClick = {
                            viewModel.activeChatInput = "Was habe ich diese Woche für Fächer laut meinem Stundenplan?"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Color(0xFF1E1E1E) else Color(0xFFE2E8F0)),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text("🗓️ Stundenplan abfragen", fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = NothingWhite)
                    }
                }

                // Media Source & Send Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Custom style button to open real camera
                    Button(
                        onClick = { println("Kamera nicht verfügbar") },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Color(0xFF1E1E1E) else Color(0xFFE2E8F0)),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text("📸 KAMERA", fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = NothingWhite)
                    }

                    // Custom style button to choose from gallery
                    Button(
                        onClick = { println("Galerie nicht verfügbar") },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Color(0xFF1E1E1E) else Color(0xFFE2E8F0)),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text("📁 GALERIE", fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = NothingWhite)
                    }

                    NothingTextField(
                        value = viewModel.activeChatInput,
                        onValueChange = { viewModel.activeChatInput = it },
                        label = "Schreibe dem Bot...",
                        modifier = Modifier.weight(1f),
                        trailingIcon = {
                            IconButton(onClick = {
                                if (viewModel.activeChatInput.isNotEmpty() || selectedImageState != null) {
                                    viewModel.sendChatPrompt(viewModel.activeChatInput, selectedImageState)
                                    selectedImageState = null // Clear preview after sending
                                }
                            }) {
                                Icon(Icons.Default.Send, contentDescription = StringResources.get("Senden"), tint = NothingWhite)
                            }
                        }
                    )
                }
            }
        }
    }
}


// --- SETTINGS CONFIG SCREEN ---
@Composable
fun SettingsScreen(viewModel: UntisViewModel) {
    var showComplateTeachers by remember { mutableStateOf(true) }
    var showComplateSubjects by remember { mutableStateOf(true) }
    var useColorsOfSubject by remember { mutableStateOf(true) }
    var representationChanges by remember { mutableStateOf(true) }
    var showCancellationsState by remember { mutableStateOf(true) }
    var showClassRooms by remember { mutableStateOf(true) }
    var showColorsElements by remember { mutableStateOf(true) }

    val clipboard = androidx.compose.ui.platform.LocalClipboardManager.current
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val borderColor = if (isDark) Color(0xFF222222) else Color(0xFFE2E8F0)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NothingBlack)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            NothingHeader(text = StringResources.get("Einstellungen"), fontSize = 28.sp)
        }

        // Language Section
        item {
            Surface(
                color = NothingCardGray,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, borderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(StringResources.get("Sprache / Language"), fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, color = NothingMutedGray, fontSize = 11.sp, letterSpacing = 1.sp)

                    SettingsToggleRow(
                        title = "English UI",
                        desc = StringResources.get("Wähle die App-Sprache aus"),
                        checked = StringResources.currentLanguage.value == com.example.ui.AppLanguage.EN,
                        onCheckedChange = { isEn ->
                            StringResources.currentLanguage.value = if (isEn) com.example.ui.AppLanguage.EN else com.example.ui.AppLanguage.DE
                        }
                    )
                    
                    Divider(color = borderColor)
                    
                    SettingsToggleRow(
                        title = "Stock Android Theme",
                        desc = "Nutze das Standard Android Theme / Dynamic Colors",
                        checked = viewModel.useStockThemePref,
                        onCheckedChange = { 
                            viewModel.useStockThemePref = it
                            viewModel.saveAppSettings()
                        }
                    )
                }
            }
        }

        // Notifications Management Section
        item {
            Surface(
                color = NothingCardGray,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, borderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "BENACHRICHTIGUNGEN & ALERTS",
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        color = NothingMutedGray,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )

                    SettingsToggleRow(
                        title = "Hausaufgaben-Meldungen",
                        desc = "Echtzeit-Alerts für fällige und eingetragene Hausaufgaben",
                        checked = viewModel.homeworkNotificationsEnabled,
                        onCheckedChange = {
                            viewModel.homeworkNotificationsEnabled = it
                            viewModel.saveAppSettings()
                        }
                    )

                    Divider(color = borderColor)

                    SettingsToggleRow(
                        title = "Stundenplan-Änderungen",
                        desc = "Sofort-Meldungen bei Vertretungen, Ausfällen und Raumwechseln",
                        checked = viewModel.timetableNotificationsEnabled,
                        onCheckedChange = {
                            viewModel.timetableNotificationsEnabled = it
                            viewModel.saveAppSettings()
                        }
                    )

                    Divider(color = borderColor)

                    Text(
                        text = "SIMULATOR FÜR SYSTEM-MELDUNGEN",
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        color = NothingMutedGray,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.triggerHomeworkTestAlert()
                                println("Hausaufgaben-Alert simuliert!")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Color(0xFF1E1E1E) else Color(0xFFE2E8F0), contentColor = NothingWhite),
                            border = BorderStroke(1.dp, borderColor),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.weight(1f).height(40.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("HW Testen", fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                viewModel.triggerTimetableTestAlert()
                                println("Plan Testen")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Color(0xFF1E1E1E) else Color(0xFFE2E8F0), contentColor = NothingWhite),
                            border = BorderStroke(1.dp, borderColor),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.weight(1f).height(40.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Plan Testen", fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Credentials Section
        item {
            Surface(
                color = NothingCardGray,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, borderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(StringResources.get("API & VERBINDUNG"), fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, color = NothingMutedGray, fontSize = 11.sp, letterSpacing = 1.sp)

                    NothingTextField(
                        value = viewModel.geminiApiKeyInput,
                        onValueChange = { viewModel.geminiApiKeyInput = it },
                        label = "Gemini AI API Key (für AI Chatbot)"
                    )

                    NothingTextField(
                        value = viewModel.reminderMinutesInput.toString(),
                        onValueChange = { viewModel.reminderMinutesInput = it.toIntOrNull() ?: 60 },
                        label = "Hausaufgaben Erinnerung (Minuten vorher)"
                    )

                    NothingButton(
                        text = "Keys & Einstellungen Sichern",
                        onClick = {
                            viewModel.saveAppSettings()
                            println(StringResources.get("Einstellungen gespeichert!"))
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Original Timetable Widget configuration toggles from settings screenshot
        item {
            Surface(
                color = NothingCardGray,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, borderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(StringResources.get("STUNDENPLAN ANSICHT"), fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, color = NothingMutedGray, fontSize = 11.sp, letterSpacing = 1.sp)

                    SettingsToggleRow(
                        title = "Lehrernamen ausschreiben",
                        desc = "Zeigt beim Stundenplan den vollständigen Namen an, sofern Platz ist",
                        checked = showComplateTeachers,
                        onCheckedChange = { showComplateTeachers = it }
                    )

                    Divider(color = borderColor)

                    SettingsToggleRow(
                        title = "Fachnamen ausschreiben",
                        desc = "Zeigt beim Stundenplan den vollständigen Fachnamen anstelle des Kürzels",
                        checked = showComplateSubjects,
                        onCheckedChange = { showComplateSubjects = it }
                    )

                    Divider(color = borderColor)

                    SettingsToggleRow(
                        title = "Fachfarben anzeigen",
                        desc = "Aktiviert den bunten Stundenplan mit benutzerdefinierten Farben für jedes Fach",
                        checked = useColorsOfSubject,
                        onCheckedChange = { useColorsOfSubject = it }
                    )

                    Divider(color = borderColor)

                    SettingsToggleRow(
                        title = "Vertretungskennzeichnung",
                        desc = "Vertretungen und Raumänderungen auffällig hervorheben",
                        checked = representationChanges,
                        onCheckedChange = { representationChanges = it }
                    )

                    Divider(color = borderColor)

                    SettingsToggleRow(
                        title = "Entfälle anzeigen",
                        desc = "Ausgefallene Unterrichtsstunden durchgestrichen anzeigen",
                        checked = showCancellationsState,
                        onCheckedChange = { showCancellationsState = it }
                    )

                    Divider(color = borderColor)

                    SettingsToggleRow(
                        title = "Räume einblenden",
                        desc = "Zeigt die jeweilige Raumnummer im Stundenplanfeld an",
                        checked = showClassRooms,
                        onCheckedChange = { showClassRooms = it }
                    )

                    Divider(color = borderColor)

                    SettingsToggleRow(
                        title = "Farben aus WebUntis",
                        desc = "Synchronisiert die in WebUntis hinterlegten Standardfarben",
                        checked = showColorsElements,
                        onCheckedChange = { showColorsElements = it }
                    )
                }
            }
        }
        
        item {
            Surface(
                color = NothingCardGray,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, borderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = StringResources.get("ICS KALENDERABONNEMENT"),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        color = NothingMutedGray,
                        letterSpacing = 1.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.exportCalendarSubscription()
                                println(StringResources.get("Stundenplan lokal als ICS exportiert (Cache)!"))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NothingWhite, contentColor = NothingBlack),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.weight(1f).height(40.dp)
                        ) {
                            Text(StringResources.get("ICS DATEI EXPORT"), fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                val link = viewModel.getIcsCalendarSubscriptionLink()
                                clipboard.setText(AnnotatedString(link))
                                println(StringResources.get("ICS Link in die Zwischenablage kopiert! Trage diesen im Google Kalender ein."))
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NothingWhite),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, NothingWhite.copy(alpha = 0.25f)),
                            modifier = Modifier.weight(1f).height(40.dp)
                        ) {
                            Text(StringResources.get("ICS LINK KOPIEREN"), fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // System & Updates Section
        item {
            Surface(
                color = NothingCardGray,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, borderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "SYSTEM & UPDATES",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        color = NothingMutedGray,
                        letterSpacing = 1.sp
                    )

                    NothingButton(
                        text = "Nach Updates suchen",
                        onClick = {
                            viewModel.checkForUpdatesManual()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        isPrimary = false
                    )
                }
            }
        }

        // About / Log out
        item {
            NothingButton(
                text = "Ausloggen & Reset",
                onClick = { viewModel.logout() },
                modifier = Modifier.fillMaxWidth(),
                isPrimary = false
            )
        }
    }
}

@Composable
fun SettingsToggleRow(
    title: String,
    desc: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontFamily = FontFamily.Monospace, color = NothingWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(desc, fontFamily = FontFamily.Monospace, color = NothingMutedGray, fontSize = 10.sp, lineHeight = 13.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = NothingWhite,
                checkedTrackColor = NothingRed,
                uncheckedThumbColor = NothingMutedGray,
                uncheckedTrackColor = NothingDarkGray
            )
        )
    }
}

// --- INFO SCREEN ---
@Composable
fun InfoScreen(viewModel: UntisViewModel) {
    var clickCount by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NothingBlack)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            NothingHeader(text = "INFO & CREDITS", fontSize = 28.sp)
            IconButton(
                onClick = { viewModel.currentScreen = "PROFILE" },
                modifier = Modifier.background(NothingCardGray, CircleShape)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Close Info", tint = NothingWhite)
            }
        }

        Surface(
            color = NothingCardGray,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "UNTIS NEO",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = NothingWhite,
                    fontSize = 20.sp
                )
                
                Text(
                    text = "Version 1.0.0 (BETA)\nDie inoffizielle, schönere Alternative zum Schulalltag.",
                    fontFamily = FontFamily.Monospace,
                    color = NothingMutedGray,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Divider(color = Color(0xFF333333))

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Macher",
                        fontFamily = FontFamily.Monospace,
                        color = NothingMutedGray,
                        fontSize = 14.sp
                    )
                    
                    Text(
                        text = "norobb",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = if (clickCount >= 4) NothingRed else NothingWhite,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .clickable {
                                clickCount++
                                if (clickCount >= 7) {
                                    clickCount = 0
                                    viewModel.currentScreen = "ARCADE"
                                }
                            }
                            .padding(8.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Design System",
                        fontFamily = FontFamily.Monospace,
                        color = NothingMutedGray,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "NDot / Nothing",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = NothingWhite,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
