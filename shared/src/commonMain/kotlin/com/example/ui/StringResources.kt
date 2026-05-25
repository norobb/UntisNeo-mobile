package com.example.ui

import androidx.compose.runtime.mutableStateOf

enum class AppLanguage { DE }

object StringResources {
    var currentLanguage = mutableStateOf(AppLanguage.DE)

    private val translations = mapOf(
        AppLanguage.DE to mapOf(
            "Untis Neo" to "Untis Neo",
            "Die schönere Oberfläche für deinen Schulalltag." to "Die schönere Oberfläche für deinen Schulalltag.",
            "ANMELDUNG" to "ANMELDUNG",
            "Demo-Modus aktivieren" to "Demo-Modus aktivieren",
            "Lädt die echten Screenshots-Daten zur Vorschau!" to "Zeigt echte Demodaten zur Vorschau!",
            "Bitte gib einen Benutzernamen ein oder starte den Demo-Modus!" to "Bitte Name eingeben oder Demo-Modus starten!",
            "LERNPLATZ" to "LERNPLATZ",
            "HOMEWORK" to "HAUSAUFGABEN",
            "Aufgaben offen" to "Aufgaben offen",
            "Fehlstunden (0 krank)" to "Fehlstunden (0 krank)",
            "Sprechstunden geladen." to "Sprechstunden geladen.",
            "Lehrer Sprechstunden" to "Lehrer Sprechstunden",
            "Schulferien anzeigen..." to "Schulferien anzeigen...",
            "Ferien & Feiertage" to "Ferien & Feiertage",
            "AI HAUSAUFGABEN HELFER" to "KI HAUSAUFGABEN HELFER",
            "Fotografiere dein Buch oder Arbeitsblatt, um Hausaufgaben direkt einzutragen!" to "Fotografiere dein Buch oder Arbeitsblatt, um Hausaufgaben direkt einzutragen!",
            "Gelehrtenschule" to "Gelehrtenschule",
            "Keine Stunden für diesen Tag." to "Keine Stunden für diesen Tag.",
            "NORMAL" to "NORMAL",
            "ENTFÄLLT" to "ENTFÄLLT",
            "Raumänderung" to "Raumänderung",
            "ICS KALENDERABONNEMENT" to "ICS KALENDERABO",
            "Stundenplan lokal als ICS exportiert (Cache)!" to "Stundenplan als ICS exportiert!",
            "ICS DATEI EXPORT" to "ICS DATEI EXPORT",
            "ICS Link in die Zwischenablage kopiert! Trage diesen im Google Kalender ein." to "ICS Link kopiert!",
            "ICS LINK KOPIEREN" to "ICS LINK KOPIEREN",
            "Mitteilungen" to "Mitteilungen",
            "Inhalt oder Person suchen" to "Inhalt oder Person suchen",
            "Keine Nachrichten vorhanden." to "Keine Nachrichten vorhanden.",
            "Nachricht schreiben" to "Nachricht schreiben",
            "NEUE NACHRICHT" to "NEUE NACHRICHT",
            "Bestätigen" to "Bestätigen",
            "Senden" to "Senden",
            "Hausaufgaben" to "Hausaufgaben",
            "Nur ungelöste Aufgaben" to "Nur offene",
            "Sehr schön, keine Hausaufgaben ausstehend!" to "Keine Hausaufgaben ausstehend!",
            "PRIVAT" to "PRIVAT",
            "EIGENE HAUSAUFGABE" to "EIGENE HAUSAUFGABE",
            "Abbrechen" to "Abbrechen",
            "Hinzufügen" to "Hinzufügen",
            "Notenspiegel" to "Notenverwaltung",
            "NOTENSCHNITT" to "NOTENSCHNITT",
            "Unter Berücksichtigung aller Gewichtungen" to "Unter Berücksichtigung aller Gewicht",
            "0-15 PUNKTE" to "0-15 PUNKTE",
            "1-6 SCHULNOTEN" to "1-6 SCHULNOTEN",
            "Noch keine Noten eingetragen." to "Noch keine Noten.",
            "Löschen" to "Löschen",
            "NEUE NOTE EINTRAGEN" to "NEUE NOTE",
            "Eintragen" to "Eintragen",
            "KI-Hausaufgaben" to "KI-Hausaufgaben",
            "Lade Hausaufgabenscreenshots hoch & die KI trägt sie strukturiert ein!" to "Lade Bilder hoch & die KI wertet sie aus!",
            "Analysiere Hausaufgaben mit Gemini..." to "Analysiere Hausaufgaben...",
            "Screenshot aus Galerie ausgewählt!" to "Bild ausgewählt!",
            "MOCK SCREENSHOT HOCHLADEN" to "BILD HOCHLADEN",
            "Einstellungen" to "Einstellungen",
            "API & VERBINDUNG" to "API & VERBINDUNG",
            "Einstellungen gespeichert!" to "Gespeichert!",
            "STUNDENPLAN ANSICHT" to "STUNDENPLAN",
            "Home" to "Start",
            "Timetable" to "Stundenplan",
            "Messages" to "Nachrichten",
            "Profile" to "Profil",
            "Zwangssync" to "Sync",
            "Sprache / Language" to "Sprache",
            "Wähle die App-Sprache aus" to "Wähle die App-Sprache aus"
        )
    )

    fun get(key: String): String {
        return translations[currentLanguage.value]?.get(key) ?: key
    }
}
