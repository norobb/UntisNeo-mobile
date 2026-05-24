package com.example.utils

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

data class UpdateInfo(
    val available: Boolean,
    val newVersion: String,
    val downloadUrl: String,
    val releaseNotes: String
)

object AutoUpdater {
    private const val TAG = "AutoUpdater"
    private const val REPO_URL = "https://api.github.com/repos/norobb/UntisNeo/releases/latest"

    suspend fun checkForUpdates(): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val url = URL(REPO_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json")

            if (connection.responseCode == 200) {
                val response = connection.inputStream.bufferedReader().readText()
                val json = JSONObject(response)
                
                val tagName = json.optString("tag_name", "").replace("v", "")
                val releaseNotes = json.optString("body", "Neue Version verfügbar.")
                
                val assets = json.optJSONArray("assets")
                var downloadUrl = ""
                if (assets != null) {
                    for (i in 0 until assets.length()) {
                        val asset = assets.getJSONObject(i)
                        val name = asset.optString("name", "")
                        if (name.endsWith(".apk")) {
                            downloadUrl = asset.optString("browser_download_url", "")
                            break
                        }
                    }
                }

                // Simple version check
                val currentVersion = BuildConfig.VERSION_NAME.replace("v", "")
                val isNewer = isVersionNewer(currentVersion, tagName)
                
                return@withContext UpdateInfo(
                    available = isNewer && downloadUrl.isNotEmpty(),
                    newVersion = tagName,
                    downloadUrl = downloadUrl,
                    releaseNotes = releaseNotes
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check for updates", e)
        }
        return@withContext null
    }

    private fun isVersionNewer(current: String, fetched: String): Boolean {
        return try {
            val currParts = current.split(".").map { it.toIntOrNull() ?: 0 }
            val fetchParts = fetched.split(".").map { it.toIntOrNull() ?: 0 }
            for (i in 0 until maxOf(currParts.size, fetchParts.size)) {
                val c = currParts.getOrElse(i) { 0 }
                val f = fetchParts.getOrElse(i) { 0 }
                if (f > c) return true
                if (f < c) return false
            }
            false
        } catch (e: Exception) {
            current != fetched
        }
    }

    fun downloadAndInstall(context: Context, url: String, fileName: String = "untisneo_update.apk") {
        try {
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val uri = Uri.parse(url)
            
            // Delete old update file if exists
            val oldFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
            if (oldFile.exists()) {
                oldFile.delete()
            }

            val request = DownloadManager.Request(uri).apply {
                setTitle("UntisNeo Update")
                setDescription("Lade Update herunter...")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, fileName)
            }

            val downloadId = downloadManager.enqueue(request)

            val onComplete = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                    if (downloadId == id) {
                        installApk(context, fileName)
                        context.unregisterReceiver(this)
                    }
                }
            }
            
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), Context.RECEIVER_EXPORTED)
            } else {
                context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Download failed", e)
        }
    }

    private fun installApk(context: Context, fileName: String) {
        try {
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
            if (file.exists()) {
                val uri = FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.provider", file)
                val installIntent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/vnd.android.package-archive")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                }
                context.startActivity(installIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Install failed", e)
        }
    }
}
