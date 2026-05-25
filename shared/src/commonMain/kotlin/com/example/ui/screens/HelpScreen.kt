package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.UntisViewModel
import com.example.ui.components.*

@Composable
fun HelpScreen(viewModel: UntisViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NothingBlack)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
            IconButton(onClick = { viewModel.currentScreen = "SETTINGS" }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Zurück", tint = NothingWhite)
            }
            NothingHeader(text = "Hilfe & Tutorials", fontSize = 24.sp)
        }

        HelpCard(
            title = "Untis Passwort einrichten",
            content = "1. Logge dich im Browser auf der WebUntis Seite deiner Schule ein.\n" +
                      "2. Klicke auf dein Profilbild -> 'Profil'.\n" +
                      "3. Gehe zum Reiter 'Passwort'.\n" +
                      "4. Gib dort dein bestehendes WebUntis-Passwort ein oder setze ein neues für die App-Nutzung."
        )

        HelpCard(
            title = "Gemini API Key erstellen",
            content = "1. Gehe auf https://aistudio.google.com/\n" +
                      "2. Logge dich mit deinem Google Account ein.\n" +
                      "3. Klicke auf 'Get API key' und erstelle einen neuen Key.\n" +
                      "4. Kopiere den Key und trage ihn hier in der App unter 'Einstellungen -> API & VERBINDUNG' ein."
        )
        
        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun HelpCard(title: String, content: String) {
    Surface(
        color = NothingCardGray,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, color = NothingWhite, fontSize = 16.sp)
            Text(content, color = NothingMutedGray, fontSize = 14.sp)
        }
    }
}
