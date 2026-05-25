# 🎓 UntisNeo

> **Der modernste, native Android Stundenplan-Client mit Blur Design.**

UntisNeo ist eine revolutionäre Open-Source-Alternative zur klassischen Stundenplan-App. Sie verbindet ein atemberaubendes, transluzentes Blur Design mit künstlicher Intelligenz (Google Gemini) und echtem WebUntis-Hausaufgaben-Sync – exklusiv für Android!

<br>

<div align="center">
  <!-- Screenshots can be placed here in the future -->
  <img src="https://via.placeholder.com/250x500.png?text=UntisNeo+Timetable" width="250" />
  &nbsp;&nbsp;&nbsp;&nbsp;
  <img src="https://via.placeholder.com/250x500.png?text=UntisNeo+Chatbot" width="250" />
</div>

<br>

## ✨ Features

- 📱 **Nativ & Performant**: Entwickelt in Jetpack Compose für ein extrem flüssiges und stabiles Android-Erlebnis.
- 🎨 **Blur Design**: Wunderschöne, transluzente Unterrichtskarten mit echten nativen Unschärfe-Effekten (`Modifier.blur`), die sich dynamisch anpassen.
- 📚 **Echter Hausaufgaben-Sync**: Zieht (im Gegensatz zu anderen Third-Party-Clients) echte Hausaufgaben direkt über die JSON-RPC API aus WebUntis, inklusive Dringlichkeits-Badges und Icons.
- 🤖 **Neo Smart-Assistant (Gemini)**: Integrierter KI-Chatbot, der deinen Stundenplan versteht. Frage nach Hausaufgaben, Vertretungen oder lade Fotos deiner Aufgabenblätter hoch!
- 🔄 **Smart Background Sync**: Überprüft den Stundenplan automatisch im Hintergrund. Du erhältst nur dann eine Push-Benachrichtigung, wenn wirklich etwas ausfällt oder verschoben wird.
- 📡 **Meshtastic / P2P**: Teile Hausaufgaben offline per Bluetooth oder LoRa-Mesh-Netzwerk direkt mit deinen Klassenkameraden.

## 🚀 Installation

Dank GitHub Actions wird bei jedem neuen Release vollautomatisch eine `UntisNeo.apk` kompiliert. 

1. Gehe auf die [Releases-Seite](https://github.com/norobb/UntisNeo-mobile/releases).
2. Lade die neueste `UntisNeo.apk` herunter.
3. Öffne die Datei auf deinem Smartphone und installiere sie (ggf. "Installation aus unbekannten Quellen" zulassen).
4. **Auto-Updates:** Die App prüft beim Start automatisch auf neue Versionen und öffnet direkt den Download, falls verfügbar!

*(Suchst du nach der iOS-Version? Der veraltete KMP-Code für iOS befindet sich im Branch `ios-support`)*

## 🛠️ Für Entwickler

Das Projekt nutzt **Jetpack Compose**.
- `shared/`: Die gesamte UI (Compose), Datenbank (Room/SQLite), API (Ktor) und Logik.
- `androidApp/`: Der Android-spezifische App-Wrapper.

### Lokales Kompilieren
```bash
# Android Debug APK bauen
./gradlew assembleDebug
```

## 🤝 Credits

Ein besonderer Dank geht an das [UntisPlus](https://github.com/lucas-m20/UntisPlus) Projekt, welches als starke Inspiration für das UI-Konzept dieser App gedient hat!

---
*Disclaimer: UntisNeo ist ein inoffizieller Client und steht in keiner Verbindung zur Untis GmbH.*