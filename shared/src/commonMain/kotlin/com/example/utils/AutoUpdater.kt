package com.example.utils

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

data class UpdateInfo(
    val available: Boolean,
    val newVersion: String,
    val downloadUrl: String,
    val releaseNotes: String
)

object AutoUpdater {
    private const val REPO_URL = "https://api.github.com/repos/norobb/UntisNeo/releases/latest"
    private const val CURRENT_VERSION = "1.0.0" // Will be replaced per platform

    private val client = HttpClient {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    }

    suspend fun checkForUpdates(): UpdateInfo? = withContext(Dispatchers.Default) {
        try {
            val response = client.get(REPO_URL) {
                header("Accept", "application/vnd.github.v3+json")
            }
            val body = response.bodyAsText()
            val json = Json.parseToJsonElement(body).jsonObject

            val tagName = json["tag_name"]?.jsonPrimitive?.content?.replace("v", "") ?: ""
            val releaseNotes = json["body"]?.jsonPrimitive?.content ?: "Neue Version verfügbar."

            val assets = json["assets"]?.jsonArray
            var downloadUrl = ""
            assets?.forEach { asset ->
                val name = asset.jsonObject["name"]?.jsonPrimitive?.content ?: ""
                if (name.endsWith(".ipa") || name.endsWith(".apk")) {
                    downloadUrl = asset.jsonObject["browser_download_url"]?.jsonPrimitive?.content ?: ""
                    return@forEach
                }
            }

            val isNewer = isVersionNewer(CURRENT_VERSION, tagName)
            UpdateInfo(
                available = isNewer && downloadUrl.isNotEmpty(),
                newVersion = tagName,
                downloadUrl = downloadUrl,
                releaseNotes = releaseNotes
            )
        } catch (e: Exception) {
            println("[AutoUpdater] Failed to check for updates: ${e.message}")
            null
        }
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

    // Download/install is platform-specific - no-op in commonMain
    fun downloadAndInstall(url: String) {
        println("[AutoUpdater] downloadAndInstall not available on this platform. URL: $url")
    }
}
