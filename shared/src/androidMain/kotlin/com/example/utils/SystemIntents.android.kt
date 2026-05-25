package com.example.utils

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import com.myapplication.AppGlobals

actual object SystemIntents {
    actual fun openUrl(url: String) {
        val context = AppGlobals.appContext ?: return
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    actual fun downloadAndInstallApk(url: String) {
        val context = AppGlobals.appContext ?: return
        try {
            val request = DownloadManager.Request(Uri.parse(url))
                .setTitle("UntisNeo Update")
                .setDescription("Downloading latest APK...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "UntisNeo_update.apk")

            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)
            // Note: Installation requires a BroadcastReceiver listening for DOWNLOAD_COMPLETE.
            // For simplicity, we just trigger the download and let the user tap the notification.
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to opening in browser
            openUrl(url)
        }
    }
}
