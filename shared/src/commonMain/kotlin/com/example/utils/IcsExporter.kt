package com.example.utils

expect object IcsExporter {
    fun openIcsInCalendar(icsData: String, title: String)
}
