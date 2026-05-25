package com.example.data.api

import com.example.data.room.Homework
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

object GeminiService {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
            })
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    suspend fun analyzeHomework(
        textPrompt: String,
        bitmapBytes: ByteArray?,
        userApiKey: String,
        buildConfigKey: String,
        studentName: String = "Schüler",
        homeworksContext: String = "",
        lessonsContext: String = ""
    ): HomeworkResult = withContext(Dispatchers.Default) {
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

            // 1. Text part (Prompt + Instructions hint)
            val promptText = if (textPrompt.isNotEmpty()) {
                "$textPrompt\n\n(Das heutige Datum ist der 23. Mai 2026. Antworte in JSON wie instruiert.)"
            } else {
                "Analysiere dieses Bild und trage eventuelle Hausaufgaben ein oder beantworte die Fragen im Bild."
            }

            val requestJson = buildJsonObject {
                put("contents", buildJsonArray {
                    add(buildJsonObject {
                        put("parts", buildJsonArray {
                            add(buildJsonObject {
                                put("text", promptText)
                            })
                            // 2. Image part if present
                            if (bitmapBytes != null) {
                                add(buildJsonObject {
                                    putJsonObject("inlineData") {
                                        put("mimeType", "image/jpeg")
                                        put("data", Base64.Default.encode(bitmapBytes))
                                    }
                                })
                            }
                        })
                    })
                })
                
                put("systemInstruction", buildJsonObject {
                    put("parts", buildJsonArray {
                        add(buildJsonObject {
                            put("text", instructions)
                        })
                    })
                })

                put("generationConfig", buildJsonObject {
                    put("responseMimeType", "application/json")
                    put("temperature", 0.4)
                })
            }

            val url = "$BASE_URL?key=$apiKey"
            val response: HttpResponse = client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(requestJson.toString())
            }
            
            if (response.status.value !in 200..299) {
                val errorBody = response.bodyAsText()
                println("GeminiService API call failed: $errorBody")
                return@withContext HomeworkResult.Error("API-Aufruf fehlgeschlagen: Code ${response.status.value}. Details: $errorBody")
            }

            val responseBody = response.bodyAsText()
            if (responseBody.isEmpty()) return@withContext HomeworkResult.Error("Leere Antwort erhalten.")
            println("GeminiService Raw Response: $responseBody")

            val jsonResponse = Json.parseToJsonElement(responseBody).jsonObject
            val candidates = jsonResponse["candidates"]?.jsonArray
            val firstCandidate = candidates?.get(0)?.jsonObject
            val responseText = firstCandidate?.get("content")?.jsonObject?.get("parts")?.jsonArray?.get(0)?.jsonObject?.get("text")?.jsonPrimitive?.content ?: ""

            val cleanJson = responseText.trim().removeSurrounding("```json", "```").trim()

            val parsedResult = try {
                val outerJson = Json.parseToJsonElement(cleanJson).jsonObject
                val type = outerJson["responseType"]?.jsonPrimitive?.content ?: "CHAT"
                val replyText = outerJson["replyText"]?.jsonPrimitive?.content ?: ""

                if (type == "HOMEWORK") {
                    val hwObj = outerJson["homework"]?.jsonObject
                    if (hwObj != null) {
                        val subjectCode = hwObj["subjectCode"]?.jsonPrimitive?.content ?: "Ma"
                        val description = hwObj["description"]?.jsonPrimitive?.content ?: ""
                        val dueDate = hwObj["dueDate"]?.jsonPrimitive?.content ?: "2026-05-25"
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
            println("GeminiService Error processing Gemini API homework extraction: ${e.message}")
            HomeworkResult.Error("Fehler bei der KI-Analyse: ${e.message}")
        }
    }
}

sealed class HomeworkResult {
    data class Success(val homework: Homework, val replyText: String) : HomeworkResult()
    data class ChatReply(val replyText: String) : HomeworkResult()
    data class Error(val message: String) : HomeworkResult()
}
