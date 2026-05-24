package com.example.data.api

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.data.room.Homework
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

object GeminiService {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    // Helper to convert Bitmap to Base64
    private fun Bitmap.toBase64(): String {
        val outputStream = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    suspend fun analyzeHomework(
        textPrompt: String,
        bitmap: Bitmap?,
        userApiKey: String,
        buildConfigKey: String,
        studentName: String = "Schüler",
        homeworksContext: String = "",
        lessonsContext: String = ""
    ): HomeworkResult = withContext(Dispatchers.IO) {
        // Source key selection
        val apiKey = userApiKey.trim().ifEmpty { buildConfigKey.trim() }
        if (apiKey.isEmpty() || apiKey.startsWith("MY_GEMINI_API_KEY")) {
            return@withContext HomeworkResult.Error("API Key ist nicht konfiguriert. Bitte trage deinen Gemini API Key in den Einstellungen ein.")
        }

        try {
            // Build system instructions with rich student context
            val instructions = "Du bist 'Untis Neo Smart-Assistant', ein intelligenter, humorvoller und hilfsbereiter Schul-Assistent für Schüler.\n" +
                    "Hier ist der aktuelle Kontext des Schülers:\n" +
                    "Name: $studentName\n" +
                    "Aktuelle unerledigte Hausaufgaben des Schülers:\n$homeworksContext\n" +
                    "Stundenplan des Schülers für diese Woche:\n$lessonsContext\n\n" +
                    "Der Schüler kann mit dir plaudern, Fragen stellen (z.B. 'Erkläre mir Photosynthese'), Hausaufgaben abfragen (z.B. 'Was habe ich auf?'), ODER neue Hausaufgaben eintragen (per Texteingabe oder Bildaufnahmen).\n" +
                    "Entscheide basierend auf der Eingabe, ob der Benutzer eine Hausaufgabe eintragen/erstellen möchte oder nur chatten/fragen möchte.\n" +
                    "Antworte IMMER im folgenden JSON-Format ohne Markdown-Formatierungen:\n" +
                    "{\n" +
                    "  \"responseType\": \"CHAT\", // oder \"HOMEWORK\" wenn eine neue Hausaufgabe erstellt werden soll\n" +
                    "  \"replyText\": \"Deine ausführliche Antwort auf Deutsch (Erklärung der Frage, Stundenplan-Auskunft oder prägnante Erfolgsmeldung beim Eintragen)\",\n" +
                    "  \"homework\": { // nur wenn responseType = \"HOMEWORK\"\n" +
                    "    \"subjectCode\": \"Ma\", // z.B. Ma, E, D, Ch, Bio, Phy, L, Ge (muss ein gültiges Fachkürzel sein)\n" +
                    "    \"description\": \"Hausaufgabenbeschreibung\",\n" +
                    "    \"dueDate\": \"2026-05-25\" // fälliges Datum im Format YYYY-MM-DD berechnet ab heute\n" +
                    "  }\n" +
                    "}"

            val requestJson = JSONObject()
            val contentsArray = JSONArray()
            val contentObject = JSONObject()
            val partsArray = JSONArray()

            // 1. Text part (Prompt + Instructions hint)
            val promptText = if (textPrompt.isNotEmpty()) {
                "$textPrompt\n\n(Das heutige Datum ist der 23. Mai 2026. Antworte in JSON wie instruiert.)"
            } else {
                "Analysiere dieses Bild und trage eventuelle Hausaufgaben ein oder beantworte die Fragen im Bild."
            }

            val textPart = JSONObject()
            textPart.put("text", promptText)
            partsArray.put(textPart)

            // 2. Image part if present
            if (bitmap != null) {
                val imagePart = JSONObject()
                val inlineData = JSONObject()
                inlineData.put("mimeType", "image/jpeg")
                inlineData.put("data", bitmap.toBase64())
                imagePart.put("inlineData", inlineData)
                partsArray.put(imagePart)
            }

            contentObject.put("parts", partsArray)
            contentsArray.put(contentObject)
            requestJson.put("contents", contentsArray)

            // System instructions
            val systemInstruction = JSONObject()
            val sysPartsArray = JSONArray()
            val sysTextPart = JSONObject()
            sysTextPart.put("text", instructions)
            sysPartsArray.put(sysTextPart)
            systemInstruction.put("parts", sysPartsArray)
            requestJson.put("systemInstruction", systemInstruction)

            // Generation config with JSON response format
            val config = JSONObject()
            config.put("responseMimeType", "application/json")
            config.put("temperature", 0.4)
            requestJson.put("generationConfig", config)

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = requestJson.toString().toRequestBody(mediaType)

            val url = "$BASE_URL?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: ""
                Log.e("GeminiService", "API call failed: $errorBody")
                return@withContext HomeworkResult.Error("API-Aufruf fehlgeschlagen: Code ${response.code}. Details: $errorBody")
            }

            val responseBody = response.body?.string() ?: return@withContext HomeworkResult.Error("Leere Antwort erhalten.")
            Log.d("GeminiService", "Raw Response: $responseBody")

            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.getJSONArray("candidates")
            val firstCandidate = candidates.getJSONObject(0)
            val responseText = firstCandidate.getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text")

            val cleanJson = responseText.trim().removeSurrounding("```json", "```").trim()

            val parsedResult = try {
                val outerJson = JSONObject(cleanJson)
                val type = outerJson.optString("responseType", "CHAT")
                val replyText = outerJson.optString("replyText", "")

                if (type == "HOMEWORK") {
                    val hwObj = outerJson.optJSONObject("homework")
                    if (hwObj != null) {
                        val subjectCode = hwObj.optString("subjectCode", "Ma")
                        val description = hwObj.optString("description", "")
                        val dueDate = hwObj.optString("dueDate", "2026-05-25")
                        HomeworkResult.Success(
                            Homework(
                                subjectCode = subjectCode,
                                description = description,
                                dueDate = dueDate,
                                isCustom = true,
                                isDone = false
                            ),
                            replyText = replyText.ifEmpty { "Ich habe diese Hausaufgabe eingetragen!" }
                        )
                    } else {
                        HomeworkResult.ChatReply(replyText.ifEmpty { "Plauder-Antwort." })
                    }
                } else {
                    HomeworkResult.ChatReply(replyText)
                }
            } catch (je: Exception) {
                // Not standard JSON response format, return direct text response
                HomeworkResult.ChatReply(responseText)
            }

            parsedResult
        } catch (e: Exception) {
            Log.e("GeminiService", "Error processing Gemini API homework extraction: ${e.localizedMessage}")
            HomeworkResult.Error("Fehler bei der KI-Analyse: ${e.localizedMessage}")
        }
    }
}

sealed class HomeworkResult {
    data class Success(val homework: Homework, val replyText: String) : HomeworkResult()
    data class ChatReply(val replyText: String) : HomeworkResult()
    data class Error(val message: String) : HomeworkResult()
}
