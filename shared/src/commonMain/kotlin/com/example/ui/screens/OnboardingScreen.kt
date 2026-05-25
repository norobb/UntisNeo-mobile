package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.UntisViewModel
import com.example.ui.components.NothingBlack
import com.example.ui.components.NothingButton
import com.example.ui.components.NothingCardGray
import com.example.ui.components.NothingHeader
import com.example.ui.components.NothingMutedGray
import com.example.ui.components.NothingRed
import com.example.ui.components.NothingTextField
import com.example.ui.components.NothingWhite

@Composable
fun OnboardingScreen(viewModel: UntisViewModel) {
    var currentPage by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NothingBlack)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        // Progress Indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            for (i in 0..3) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (i == currentPage) 12.dp else 8.dp)
                        .background(
                            color = if (i == currentPage) NothingRed else NothingMutedGray,
                            shape = androidx.compose.foundation.shape.CircleShape
                        )
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        when (currentPage) {
            0 -> LanguageSetupPage()
            1 -> WelcomePage()
            2 -> GeminiSetupPage(viewModel)
            3 -> FinishPage()
        }

        Spacer(modifier = Modifier.weight(1f))

        // Navigation Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (currentPage > 0) {
                TextButton(onClick = { currentPage-- }) {
                    Text("Zurück", color = NothingMutedGray, fontFamily = FontFamily.SansSerif)
                }
            } else {
                Spacer(modifier = Modifier.width(80.dp))
            }

            if (currentPage < 3) {
                NothingButton(
                    text = "Weiter",
                    onClick = { currentPage++ },
                    modifier = Modifier.width(120.dp)
                )
            } else {
                NothingButton(
                    text = "Los geht's!",
                    onClick = {
                        viewModel.hasCompletedOnboarding = true
                        viewModel.saveAppSettings()
                    },
                    modifier = Modifier.width(140.dp)
                )
            }
        }
    }
}

@Composable
fun LanguageSetupPage() {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Sprache / Language",
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Black,
            fontSize = 28.sp,
            color = NothingWhite,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        
        val languages = listOf("Deutsch (DE)" to com.example.ui.AppLanguage.DE, "English (EN)" to com.example.ui.AppLanguage.EN)
        
        languages.forEach { (name, enumVal) ->
            val isSelected = com.example.ui.StringResources.currentLanguage.value == enumVal
            Surface(
                color = if (isSelected) NothingRed.copy(alpha = 0.2f) else NothingCardGray,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, if (isSelected) NothingRed else Color.Transparent),
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    .androidx.compose.foundation.clickable {
                        com.example.ui.StringResources.currentLanguage.value = enumVal
                    }
            ) {
                Text(
                    text = name,
                    color = NothingWhite,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun WelcomePage() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Willkommen bei\nUntisNeo",
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Black,
            fontSize = 32.sp,
            color = NothingWhite,
            textAlign = TextAlign.Center,
            lineHeight = 36.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Dein smarter Schul-Assistent.",
            fontFamily = FontFamily.Monospace,
            color = NothingRed,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(32.dp))
        Surface(
            color = NothingCardGray,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FeatureRow("📱", "Modernes, dynamisches Design")
                FeatureRow("📡", "Offline-Radar & P2P Messaging")
                FeatureRow("🤖", "KI-gestützte Hausaufgaben")
                FeatureRow("🔄", "Automatische Updates")
            }
        }
    }
}

@Composable
fun FeatureRow(emoji: String, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(emoji, fontSize = 20.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Text(text, color = NothingWhite, fontFamily = FontFamily.SansSerif, fontSize = 14.sp)
    }
}

@Composable
fun GeminiSetupPage(viewModel: UntisViewModel) {
    val uriHandler = LocalUriHandler.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Neo KI-Assistent",
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Black,
            fontSize = 28.sp,
            color = NothingWhite
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Um den Chatbot und die automatische Hausaufgaben-Erkennung per Foto nutzen zu können, benötigst du einen kostenlosen Gemini API Key von Google.",
            fontFamily = FontFamily.SansSerif,
            color = NothingMutedGray,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
        Spacer(modifier = Modifier.height(32.dp))
        
        NothingButton(
            text = "API Key holen (Google AI Studio)",
            onClick = {
                uriHandler.openUri("https://aistudio.google.com/app/apikey")
            },
            modifier = Modifier.fillMaxWidth(),
            isPrimary = false
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        NothingTextField(
            value = viewModel.geminiApiKeyInput,
            onValueChange = { viewModel.geminiApiKeyInput = it },
            label = "Dein Gemini API Key"
        )
    }
}

@Composable
fun FinishPage() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Alles bereit!",
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Black,
            fontSize = 32.sp,
            color = NothingWhite
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Du kannst jetzt sofort loslegen.\nDein Stundenplan, deine Noten und der KI-Chatbot warten auf dich.",
            fontFamily = FontFamily.SansSerif,
            color = NothingMutedGray,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Tipp: Du kannst Einstellungen und den API Key später jederzeit im Profil ändern.",
            fontFamily = FontFamily.Monospace,
            color = NothingRed,
            fontSize = 11.sp,
            textAlign = TextAlign.Center
        )
    }
}
