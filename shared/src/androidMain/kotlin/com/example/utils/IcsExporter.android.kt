package com.example.utils

import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider

import java.io.File

actual object IcsExporter {
    actual fun openIcsInCalendar(icsData: String, title: String) {
        val context = AppGlobals.appContext ?: return
        try {
            val file = File(context.cacheDir, "$title.ics")
            file.writeText(icsData)

            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "text/calendar")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            println("IcsExporter: Failed to export/open ICS on Android.")
        }
    }
}
