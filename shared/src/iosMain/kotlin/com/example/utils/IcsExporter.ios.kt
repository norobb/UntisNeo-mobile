package com.example.utils

import platform.Foundation.*
import platform.UIKit.*

actual object IcsExporter {
    actual fun openIcsInCalendar(icsData: String, title: String) {
        // Simple fallback: Print to console on iOS
        // A full implementation requires EventKit bindings or accessing the root UIViewController 
        // to present a UIActivityViewController with the written file URL.
        println("IcsExporter (iOS): Feature stubbed out in commonMain. ICS Data:\n$icsData")
    }
}
