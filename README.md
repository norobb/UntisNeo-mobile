# 🎓 UntisNeo

> **Der modernste, plattformübergreifende Stundenplan-Client. Powered by Kotlin Multiplatform.**

UntisNeo ist eine revolutionäre Open-Source-Alternative zur klassischen Stundenplan-App. Sie verbindet ein atemberaubendes "Liquid Glass" Design mit künstlicher Intelligenz (Google Gemini) und dezentralen P2P-Funktionen (Meshtastic) – **gleichzeitig für Android und iOS!**

<br>

<div align="center">
  <!-- Screenshots can be placed here in the future -->
  <img src="https://via.placeholder.com/250x500.png?text=UntisNeo+Timetable" width="250" />
  &nbsp;&nbsp;&nbsp;&nbsp;
  <img src="https://via.placeholder.com/250x500.png?text=UntisNeo+Chatbot" width="250" />
</div>

<br>

## ✨ Features

- 📱 **Cross-Platform**: Eine einzige, moderne Code-Basis (KMP & Compose Multiplatform) liefert native Apps für Android, iOS und Desktop.
- 🎨 **UntisPlus "Liquid Glass" Design**: Wunderschöne, transluzente Unterrichtskarten mit weichen Unschärfe-Effekten, die sich dynamisch anpassen.
- 🤖 **Neo Smart-Assistant (Gemini)**: Integrierter KI-Chatbot, der deinen Stundenplan versteht. Frage nach Hausaufgaben, Vertretungen oder lade Fotos deiner Aufgabenblätter hoch!
- 🔄 **Smart Background Sync**: Überprüft den Stundenplan automatisch im Hintergrund. Du erhältst nur dann eine Push-Benachrichtigung, wenn wirklich etwas ausfällt oder verschoben wird.
- 📡 **Meshtastic / P2P**: Teile Hausaufgaben offline per Bluetooth oder LoRa-Mesh-Netzwerk direkt mit deinen Klassenkameraden.

## 🚀 Installation

Dank GitHub Actions wird bei jedem neuen Release vollautomatisch eine `UntisNeo.apk` (Android) und eine `UntisNeo.ipa` (iOS) kompiliert. 

### Für Android
1. Gehe auf die [Releases-Seite](https://github.com/norobb/UntisNeo-mobile/releases).
2. Lade die neueste `UntisNeo.apk` herunter.
3. Öffne die Datei auf deinem Smartphone und installiere sie (ggf. "Installation aus unbekannten Quellen" zulassen).
4. **Auto-Updates:** Die App prüft beim Start automatisch auf neue Versionen und lädt diese direkt herunter!

### Für iOS (Sideloading)
1. Gehe auf die [Releases-Seite](https://github.com/norobb/UntisNeo-mobile/releases).
2. Lade die neueste `UntisNeo.ipa` herunter.
3. Installiere die App über **AltStore**, **Sideloadly** oder **TrollStore** auf deinem iPhone oder iPad.
*(Hinweis: Da iOS-Sideloading keine In-App-Updates erlaubt, müssen neue IPA-Versionen manuell über den AltStore installiert werden).*

## 🛠️ Für Entwickler

Das Projekt nutzt **Kotlin Multiplatform (KMP)**.
- `shared/`: Die gesamte UI (Compose), Datenbank (Room/SQLite), API (Ktor) und Logik.
- `androidApp/`: Der Android-spezifische Einstiegspunkt.
- `iosApp/`: Der iOS-spezifische Einstiegspunkt (SwiftUI Wrapper).
- `desktopApp/`: Der Desktop-Einstiegspunkt.

### Lokales Kompilieren
```bash
# Android Debug APK
./gradlew assembleDebug

# iOS Framework (wird für Xcode benötigt)
./gradlew :shared:linkReleaseFrameworkIosArm64
```

---
*Disclaimer: UntisNeo ist ein inoffizieller Client und steht in keiner Verbindung zur Untis GmbH.*