const fs = require('fs');

const mappings = [
    "Untis Neo", "Die schönere Oberfläche für deinen Schulalltag.", "ANMELDUNG", "Demo-Modus aktivieren", "Lädt die echten Screenshots-Daten zur Vorschau!", "Bitte gib einen Benutzernamen ein oder starte den Demo-Modus!", "LERNPLATZ", "HOMEWORK", "Aufgaben offen", "Fehlstunden (0 krank)", "Sprechstunden geladen.", "Lehrer Sprechstunden", "Schulferien anzeigen...", "Ferien & Feiertage", "AI HAUSAUFGABEN HELFER", "Fotografiere dein Buch oder Arbeitsblatt, um Hausaufgaben direkt einzutragen!", "Gelehrtenschule", "Keine Stunden für diesen Tag.", "NORMAL", "ENTFÄLLT", "Raumänderung", "ICS KALENDERABONNEMENT", "Stundenplan lokal als ICS exportiert (Cache)!", "ICS DATEI EXPORT", "ICS Link in die Zwischenablage kopiert! Trage diesen im Google Kalender ein.", "ICS LINK KOPIEREN", "Mitteilungen", "Inhalt oder Person suchen", "Keine Nachrichten vorhanden.", "Nachricht schreiben", "NEUE NACHRICHT", "Bestätigen", "Senden", "Hausaufgaben", "Nur ungelöste Aufgaben", "Sehr schön, keine Hausaufgaben ausstehend!", "PRIVAT", "EIGENE HAUSAUFGABE", "Abbrechen", "Hinzufügen", "Notenspiegel", "NOTENSCHNITT", "Unter Berücksichtigung aller Gewichtungen", "0-15 PUNKTE", "1-6 SCHULNOTEN", "Noch keine Noten eingetragen.", "Löschen", "NEUE NOTE EINTRAGEN", "Eintragen", "KI-Hausaufgaben", "Lade Hausaufgabenscreenshots hoch & die KI trägt sie strukturiert ein!", "Analysiere Hausaufgaben mit Gemini...", "Screenshot aus Galerie ausgewählt!", "MOCK SCREENSHOT HOCHLADEN", "Einstellungen", "API & VERBINDUNG", "Einstellungen gespeichert!", "STUNDENPLAN ANSICHT", "Home", "Timetable", "Messages", "Profile", "Zwangssync"

];

function replaceInFile(filePath) {
    let content = fs.readFileSync(filePath, 'utf-8');
    
    if (!content.includes('import com.example.ui.StringResources')) {
        content = content.replace(/(package com.example(\.[a-zA-Z0-9_]+)*)/, '$1\nimport com.example.ui.StringResources\n');
    }
    
    for (const key of mappings) {
        const safeKey = key.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
        
        // Match Text("...")
        const r1 = new RegExp(`Text\\(\\s*"${safeKey}"`, 'g');
        content = content.replace(r1, `Text(StringResources.get("${key}")`);

        // Match text = "..."
        const r2 = new RegExp(`text\\s*=\\s*"${safeKey}"`, 'g');
        content = content.replace(r2, `text = StringResources.get("${key}")`);
        
        // Match literal string inside parenthesis or comma like Toast.makeText(context, "...")
        const r3 = new RegExp(`([^\\w])"${safeKey}"([^\\w])`, 'g');
        content = content.replace(r3, `$1StringResources.get("${key}")$2`);
    }

    fs.writeFileSync(filePath, content);
    console.log(filePath + " updated.");
}

replaceInFile('app/src/main/java/com/example/ui/screens/AllScreens.kt');
replaceInFile('app/src/main/java/com/example/MainActivity.kt');
