package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.*
import com.example.ui.screens.*

/**
 * Main navigation composable for UntisNeo.
 * Routes between screens based on viewModel.currentScreen.
 */
@Composable
fun MainAppContent(viewModel: UntisViewModel) {
    val currentScreen = viewModel.currentScreen

    if (!viewModel.hasCompletedOnboarding) {
        OnboardingScreen(viewModel)
        return
    }

    if (currentScreen == "LOGON") {
        LogonScreen(viewModel)
        return
    }

    // Show update dialog if available
    val updateInfo by viewModel.updateInfo.collectAsState()
    if (updateInfo != null && updateInfo!!.available) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissUpdate() },
            title = {
                Text("Update verfügbar!", fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, color = Color.White)
            },
            text = {
                Text("Version ${updateInfo!!.newVersion} ist verfügbar.\n\n${updateInfo!!.releaseNotes}",
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFFAAAAAA),
                    fontSize = 12.sp)
            },
            confirmButton = {
                Button(onClick = {
                    com.example.utils.AutoUpdater.downloadAndInstall(updateInfo!!.downloadUrl)
                    viewModel.dismissUpdate()
                }) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissUpdate() }) {
                    Text("Später")
                }
            },
            containerColor = Color(0xFF111111),
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Main Scaffold with bottom navigation
    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF0A0A0A),
                tonalElevation = 0.dp
            ) {
                NavigationBarItem(
                    selected = currentScreen == "HOME",
                    onClick = { viewModel.currentScreen = "HOME" },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home", fontFamily = FontFamily.Monospace, fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFFF3131),
                        indicatorColor = Color(0xFF1A0000)
                    )
                )
                NavigationBarItem(
                    selected = currentScreen == "TIMETABLE",
                    onClick = { viewModel.currentScreen = "TIMETABLE" },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = "Stundenplan") },
                    label = { Text("Plan", fontFamily = FontFamily.Monospace, fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFFF3131),
                        indicatorColor = Color(0xFF1A0000)
                    )
                )
                NavigationBarItem(
                    selected = currentScreen == "HOMEWORK",
                    onClick = { viewModel.currentScreen = "HOMEWORK" },
                    icon = { Icon(Icons.Default.Edit, contentDescription = "Hausaufgaben") },
                    label = { Text("Aufgaben", fontFamily = FontFamily.Monospace, fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFFF3131),
                        indicatorColor = Color(0xFF1A0000)
                    )
                )
                NavigationBarItem(
                    selected = currentScreen == "SETTINGS",
                    onClick = { viewModel.currentScreen = "SETTINGS" },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Einstellungen") },
                    label = { Text("Optionen", fontFamily = FontFamily.Monospace, fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFFF3131),
                        indicatorColor = Color(0xFF1A0000)
                    )
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF000000))
                .padding(paddingValues)
        ) {
            when (currentScreen) {
                "HOME" -> HomeScreen(viewModel)
                "TIMETABLE" -> TimetableScreen(viewModel)
                "HOMEWORK" -> HomeworkScreen(viewModel)
                "GRADES" -> GradesScreen(viewModel)
                "MESSAGES" -> MessagesScreen(viewModel)
                "CHATBOT" -> ChatbotScreen(viewModel)
                "SETTINGS" -> SettingsScreen(viewModel)
                "ARCADE" -> ArcadeScreen(viewModel)
                else -> HomeScreen(viewModel)
            }
        }
    }
}
