package com.example.data.api

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.cookies.AcceptAllCookiesStorage
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.Url
import io.ktor.http.encodeURLQueryComponent
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.datetime.LocalDate
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.plus
import kotlinx.datetime.minus
import kotlinx.datetime.isoDayNumber
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.serialization.Serializable

@Serializable
data class SchoolSearchResult(
    val id: Int,
    val loginName: String,
    val displayName: String,
    val address: String,
    val serverUrl: String
)

import io.ktor.client.request.header

@Serializable
data class UntisClass(
    val id: Int,
    val name: String,
    val longName: String
)

class WebUntisApi {

    private val clientAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

    private val client = HttpClient {
        install(HttpCookies) {
            storage = AcceptAllCookiesStorage()
        }
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
        install(HttpTimeout) {
            connectTimeoutMillis = 15000
            requestTimeoutMillis = 15000
        }
        defaultRequest {
            header("User-Agent", clientAgent)
        }
    }

    suspend fun fetchTimetable(
        serverUrl: String, // e.g. "hh5886.webuntis.com"
        school: String, // e.g. "hh5886"
        user: String,
        pass: String,
        customType: Int? = null,
        customId: Int? = null
    ): List<com.example.data.room.TimetableLesson>? = withContext(Dispatchers.IO) {
        try {
            var cleanServerUrl = serverUrl.trim()
            if (cleanServerUrl.startsWith("http://") || cleanServerUrl.startsWith("https://")) {
                try {
                    val url = Url(cleanServerUrl)
                    val host = url.host
                    if (host.isNotEmpty()) {
                        cleanServerUrl = host
                    }
                } catch (e: Exception) {
                    // fall back to string cleaning
                }
            }
            cleanServerUrl = cleanServerUrl.replace("https://", "").replace("http://", "").trimEnd('/')
            if (cleanServerUrl.contains("/")) {
                cleanServerUrl = cleanServerUrl.split("/")[0]
            }
            println("WebUntisApi: Starting sync. Server: $cleanServerUrl, School: $school")
            val schoolEnc = school.encodeURLQueryComponent().replace("+", "%20")
            val baseUrl = "https://$cleanServerUrl/WebUntis/jsonrpc.do?school=$schoolEnc"

            // 1. Authenticate
            val authReq = buildJsonObject {
                put("id", "auth")
                put("method", "authenticate")
                put("params", buildJsonObject {
                    put("user", user)
                    put("password", pass)
                    put("client", "WebUntis") // Using default client name to ensure compatibility
                })
                put("jsonrpc", "2.0")
            }

            val authResponse = client.post(baseUrl) {
                contentType(ContentType.Application.Json)
                setBody(authReq)
            }
            
            if (!authResponse.status.isSuccess()) {
                throw Exception("HTTP-Fehler ${authResponse.status.value} bei der Anmeldung")
            }
            val authBody = authResponse.bodyAsText()
            if (authBody.isEmpty()) throw Exception("Leere Antwort bei der Anmeldung")
            
            val authJson = Json.parseToJsonElement(authBody).jsonObject
            if (authJson.containsKey("error")) {
                val errorObj = authJson["error"]?.jsonObject
                val errMsg = errorObj?.get("message")?.jsonPrimitive?.content ?: "Unbekannter Fehler"
                if (errMsg.lowercase().contains("bad credentials") || errMsg.lowercase().contains("invalid")) {
                    throw Exception("Falsche Zugangsdaten (Benutzername, Passwort oder Schule)")
                } else {
                    throw Exception("WebUntis Server-Fehler: $errMsg")
                }
            }
            
            val authResult = authJson["result"]?.jsonObject ?: throw Exception("Fehlerhafte Antwort vom WebUntis Server erhalten")
            val personType = authResult["personType"]?.jsonPrimitive?.int ?: 0
            val personId = authResult["personId"]?.jsonPrimitive?.int ?: 0

            // 2. Fetch dependencies (Subjects, Rooms, Teachers)
            val subjectsMap = fetchMap(baseUrl, "getSubjects")
            val roomsMap = fetchMap(baseUrl, "getRooms")
            val teachersMap = fetchMap(baseUrl, "getTeachers")

            // 3. Fetch Timetable for exactly 31 days starting from Monday of last week
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val daysFromMonday = today.dayOfWeek.isoDayNumber - 1
            val thisMonday = today.minus(DatePeriod(days = daysFromMonday))
            val startLocalDate = thisMonday.minus(DatePeriod(days = 7)) // Start from Monday of last week
            val endLocalDate = startLocalDate.plus(DatePeriod(days = 30)) // Add 30 days for exactly 31 days range

            val startDate = startLocalDate.year * 10000 + startLocalDate.monthNumber * 100 + startLocalDate.dayOfMonth
            val endDate = endLocalDate.year * 10000 + endLocalDate.monthNumber * 100 + endLocalDate.dayOfMonth

            println("WebUntisApi: Fetching timetable from $startDate to $endDate")

            val ttReq = buildJsonObject {
                put("id", "tt")
                put("method", "getTimetable")
                put("params", buildJsonObject {
                    put("options", buildJsonObject {
                        put("element", buildJsonObject {
                            put("id", customId ?: personId)
                            put("type", customType ?: personType)
                        })
                        put("startDate", startDate)
                        put("endDate", endDate)
                        put("showLstext", true)
                        put("showInfo", true)
                        put("showSubstText", true)
                    })
                })
                put("jsonrpc", "2.0")
            }

            val ttResponse = client.post(baseUrl) {
                contentType(ContentType.Application.Json)
                setBody(ttReq)
            }
            
            if (!ttResponse.status.isSuccess()) {
                throw Exception("HTTP-Fehler ${ttResponse.status.value} bei der Stundenplan-Abfrage")
            }
            val ttBody = ttResponse.bodyAsText()
            if (ttBody.isEmpty()) throw Exception("Leere Antwort bei der Stundenplan-Abfrage")
            
            val ttJson = Json.parseToJsonElement(ttBody).jsonObject
            
            if (ttJson.containsKey("error")) {
                val errorObj = ttJson["error"]?.jsonObject
                val errMsg = errorObj?.get("message")?.jsonPrimitive?.content ?: "Unbekannter Server-Fehler"
                throw Exception("Fehler bei Stundenplan-Abfrage: $errMsg")
            }
            
            val ttArray = ttJson["result"]?.jsonArray ?: kotlinx.serialization.json.JsonArray(emptyList())
            val lessons = mutableListOf<com.example.data.room.TimetableLesson>()

            for (i in 0 until ttArray.size) {
                val item = ttArray[i].jsonObject
                val idStr = item["id"]?.jsonPrimitive?.int?.toString() ?: ""
                
                val dateInt = item["date"]?.jsonPrimitive?.int ?: 0
                val dYear = dateInt / 10000
                val dMonth = (dateInt / 100) % 100
                val dDay = dateInt % 100
                val dateObj = LocalDate(dYear, dMonth, dDay)
                val dateStr = "${dYear}-${dMonth.toString().padStart(2, '0')}-${dDay.toString().padStart(2, '0')}"
                
                val dayOfWeekStr = dateObj.dayOfWeek.name.take(2).lowercase().replaceFirstChar { it.uppercase() }
                
                val startInt = item["startTime"]?.jsonPrimitive?.int ?: 0
                val endInt = item["endTime"]?.jsonPrimitive?.int ?: 0
                val startTimeStr = formatTime(startInt)
                val endTimeStr = formatTime(endInt)
                
                // Get subject, room, teacher
                var subjectStr = ""
                var subjectCodeStr = ""
                var roomStr = ""
                var teacherNameStr = ""
                var teacherCodeStr = ""
                
                if (item.containsKey("su") && (item["su"]?.jsonArray?.size ?: 0) > 0) {
                    val suId = item["su"]?.jsonArray?.get(0)?.jsonObject?.get("id")?.jsonPrimitive?.int
                    if (suId != null) {
                        val suObj = subjectsMap[suId]
                        if (suObj != null) {
                            subjectStr = suObj["longName"]?.jsonPrimitive?.content ?: ""
                            subjectCodeStr = suObj["name"]?.jsonPrimitive?.content ?: ""
                        }
                    }
                }
                
                if (item.containsKey("ro") && (item["ro"]?.jsonArray?.size ?: 0) > 0) {
                    val roId = item["ro"]?.jsonArray?.get(0)?.jsonObject?.get("id")?.jsonPrimitive?.int
                    if (roId != null) {
                        val roObj = roomsMap[roId]
                        if (roObj != null) {
                            roomStr = roObj["name"]?.jsonPrimitive?.content ?: ""
                        }
                    }
                }
                
                if (item.containsKey("te") && (item["te"]?.jsonArray?.size ?: 0) > 0) {
                    val teId = item["te"]?.jsonArray?.get(0)?.jsonObject?.get("id")?.jsonPrimitive?.int
                    if (teId != null) {
                        val teObj = teachersMap[teId]
                        if (teObj != null) {
                            teacherNameStr = teObj["longName"]?.jsonPrimitive?.content ?: ""
                            teacherCodeStr = teObj["name"]?.jsonPrimitive?.content ?: ""
                        }
                    }
                }
                
                var status = "NORMAL"
                if (item.containsKey("code") && item["code"]?.jsonPrimitive?.content == "cancelled") {
                    status = "CANCELLED"
                } else if (item.containsKey("code") && item["code"]?.jsonPrimitive?.content == "irregular") {
                    status = "SUBSTITUTION"
                }
                
                var info = ""
                if (item.containsKey("lstext")) info += (item["lstext"]?.jsonPrimitive?.content ?: "") + " "
                if (item.containsKey("substText")) info += (item["substText"]?.jsonPrimitive?.content ?: "") + " "
                if (item.containsKey("info")) info += (item["info"]?.jsonPrimitive?.content ?: "")
                
                // Colors based on subject string logic
                val subjectColor = getSubjectColor(subjectCodeStr)

                // Try to infer block number from startTime
                val orderBlock = inferOrder(startInt)

                lessons.add(
                    com.example.data.room.TimetableLesson(
                        id = idStr,
                        dateStr = dateStr,
                        dayOfWeek = dayOfWeekStr,
                        period = orderBlock,
                        startTime = startTimeStr,
                        endTime = endTimeStr,
                        subjectCode = subjectCodeStr.ifEmpty { "Frei" },
                        subjectName = subjectStr.ifEmpty { "Freistunde / Unbekannt" },
                        teacherCode = teacherCodeStr,
                        teacherName = teacherNameStr,
                        roomCode = roomStr,
                        colorHex = subjectColor,
                        status = status,
                        info = info.trim()
                    )
                )
            }
            
            // Logout
            val logoutReq = buildJsonObject {
                put("id", "logout")
                put("method", "logout")
                put("params", buildJsonObject {})
                put("jsonrpc", "2.0")
            }
            client.post(baseUrl) {
                contentType(ContentType.Application.Json)
                setBody(logoutReq)
            }
            
            println("WebUntisApi: Fetched ${lessons.size} lessons.")
            return@withContext lessons

        } catch (e: Exception) {
            println("WebUntisApi Exception in API: ${e.message}")
            val friendlyMsg = when {
                e.message?.startsWith("HTTP-Fehler") == true || e.message?.startsWith("WebUntis") == true || e.message?.startsWith("Falsche Zugangsdaten") == true || e.message?.startsWith("Fehler bei Stundenplan-Abfrage") == true -> e.message!!
                e.message?.contains("Unable to resolve host") == true || e.message?.contains("UnknownHost") == true || e.message?.contains("nodename nor servname") == true -> "Der Server ist nicht erreichbar. Bitte Ã¼berprÃ¼fe die Server-Adresse und deine Internetverbindung."
                e.message?.contains("timeout") == true || e.message?.contains("Timeout") == true -> "Verbindung zeitÃ¼berschritten. Der WebUntis-Server antwortet zurzeit nicht."
                e.message?.contains("Connection refused") == true || e.message?.contains("network") == true -> "Netzwerkfehler: ${e.message ?: "Verbindung abgebrochen"}"
                else -> "Fehler: ${e.message ?: "Unbekannter Fehler bei der Synchronisation"}"
            }
            throw Exception(friendlyMsg, e)
        }
    }

    private suspend fun fetchMap(baseUrl: String, method: String): Map<Int, JsonObject> {
        return try {
            val req = buildJsonObject {
                put("id", method)
                put("method", method)
                put("params", buildJsonObject {})
                put("jsonrpc", "2.0")
            }
            val response = client.post(baseUrl) {
                contentType(ContentType.Application.Json)
                setBody(req)
            }
            val body = response.bodyAsText()
            if (body.isEmpty()) return emptyMap()
            val json = Json.parseToJsonElement(body).jsonObject
            if (json.containsKey("error")) return emptyMap()
            val arr = json["result"]?.jsonArray ?: return emptyMap()
            val map = mutableMapOf<Int, JsonObject>()
            for (i in 0 until arr.size) {
                val obj = arr[i].jsonObject
                val id = obj["id"]?.jsonPrimitive?.int ?: continue
                map[id] = obj
            }
            map
        } catch (e: Exception) {
            emptyMap()
        }
    }

    private fun formatTime(timeInt: Int): String {
        // e.g. 745 -> 07:45, 1230 -> 12:30
        val h = timeInt / 100
        val m = timeInt % 100
        return "${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}"
    }

    private fun inferOrder(startInt: Int): Int {
        return when {
            startInt < 830 -> 1
            startInt < 930 -> 2
            startInt < 1030 -> 3
            startInt < 1130 -> 4
            startInt < 1230 -> 5
            startInt < 1315 -> 6
            startInt < 1400 -> 7
            startInt < 1500 -> 8
            else -> 9
        }
    }

    private fun getSubjectColor(subjectCode: String): String {
        val code = subjectCode.lowercase()
        return when {
            code.contains("ma") -> "D56BFF"
            code.contains("en") || code.contains("e") -> "4A9DFF"
            code.contains("de") || code.contains("d") -> "EF4444"
            code.contains("sp") -> "9966FF"
            code.contains("bi") -> "22C55E"
            code.contains("phy") -> "36A2EB"
            code.contains("ch") -> "4BC0C0"
            code.contains("ge") -> "EAB308"
            code.contains("ku") || code.contains("mu") -> "FF6384"
            code.contains("re") || code.contains("phi") || code.contains("eth") -> "FF9F40"
            code.contains("la") || code.contains("l") -> "14B8A6"
            code.contains("fr") -> "F43F5E"
            else -> "C9CBCF"
        }
    }

    suspend fun searchSchool(query: String): List<SchoolSearchResult> = withContext(Dispatchers.IO) {
        if (query.length < 3) return@withContext emptyList()
        try {
            val req = buildJsonObject {
                put("id", "1")
                put("method", "searchSchool")
                put("params", buildJsonObject {
                    put("search", query)
                })
                put("jsonrpc", "2.0")
            }
            // Ensure no duplicate params/search, wait WebUntis UntisPlus sends:
            // "params": [{"search": query}] -> array of objects
            val req2 = buildJsonObject {
                put("id", "1")
                put("method", "searchSchool")
                put("params", kotlinx.serialization.json.buildJsonArray {
                    add(buildJsonObject { put("search", query) })
                })
                put("jsonrpc", "2.0")
            }
            
            val response = client.post("https://mobile.webuntis.com/ms/schoolquery2") {
                contentType(ContentType.Application.Json)
                setBody(req2)
            }
            
            val body = response.bodyAsText()
            val json = Json.parseToJsonElement(body).jsonObject
            val result = json["result"]?.jsonObject
            val schoolsArray = result?.get("schools")?.jsonArray ?: return@withContext emptyList()
            
            val list = mutableListOf<SchoolSearchResult>()
            for (i in 0 until schoolsArray.size) {
                val s = schoolsArray[i].jsonObject
                list.add(
                    SchoolSearchResult(
                        id = s["schoolId"]?.jsonPrimitive?.int ?: 0,
                        loginName = s["loginName"]?.jsonPrimitive?.content ?: "",
                        displayName = s["displayName"]?.jsonPrimitive?.content ?: "",
                        address = s["address"]?.jsonPrimitive?.content ?: "",
                        serverUrl = s["server"]?.jsonPrimitive?.content ?: s["serverUrl"]?.jsonPrimitive?.content ?: ""
                    )
                )
            }
            list
        } catch (e: Exception) {
            println("WebUntisApi searchSchool error: ${e.message}")
            emptyList()
        }
    }

    suspend fun fetchClasses(
        serverUrl: String,
        school: String,
        user: String,
        pass: String
    ): List<UntisClass> = withContext(Dispatchers.IO) {
        try {
            var cleanServerUrl = serverUrl.trim()
            if (cleanServerUrl.startsWith("http://") || cleanServerUrl.startsWith("https://")) {
                cleanServerUrl = cleanServerUrl.replace("https://", "").replace("http://", "").trimEnd('/')
                if (cleanServerUrl.contains("/")) {
                    cleanServerUrl = cleanServerUrl.split("/")[0]
                }
            }
            val schoolEnc = school.encodeURLQueryComponent().replace("+", "%20")
            val baseUrl = "https://$cleanServerUrl/WebUntis/jsonrpc.do?school=$schoolEnc"

            val authReq = buildJsonObject {
                put("id", "1")
                put("method", "authenticate")
                put("params", buildJsonObject {
                    put("user", user)
                    put("password", pass)
                    put("client", "UntisNeo")
                })
                put("jsonrpc", "2.0")
            }

            val authResponse = client.post(baseUrl) {
                contentType(ContentType.Application.Json)
                setBody(authReq.toString())
            }
            
            if (!authResponse.status.isSuccess()) return@withContext emptyList()
            
            val authBody = authResponse.bodyAsText()
            val authJson = Json.parseToJsonElement(authBody).jsonObject
            val resultObj = authJson["result"]?.jsonObject
            val sessionId = resultObj?.get("sessionId")?.jsonPrimitive?.content ?: return@withContext emptyList()

            val klassenReq = buildJsonObject {
                put("id", "2")
                put("method", "getKlassen")
                put("params", buildJsonObject {})
                put("jsonrpc", "2.0")
            }

            val klassenResponse = client.post(baseUrl) {
                contentType(ContentType.Application.Json)
                header("Cookie", "JSESSIONID=$sessionId")
                setBody(klassenReq.toString())
            }

            val klassenBody = klassenResponse.bodyAsText()
            val klassenJson = Json.parseToJsonElement(klassenBody).jsonObject
            val klassenArray = klassenJson["result"]?.jsonArray ?: return@withContext emptyList()

            val list = mutableListOf<UntisClass>()
            for (element in klassenArray) {
                val obj = element.jsonObject
                list.add(
                    UntisClass(
                        id = obj["id"]?.jsonPrimitive?.int ?: 0,
                        name = obj["name"]?.jsonPrimitive?.content ?: "",
                        longName = obj["longName"]?.jsonPrimitive?.content ?: ""
                    )
                )
            }

            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    // --- Phase 4: Extended APIs (Mocked/Prep for REST /v2/ usage) ---
    
    suspend fun fetchAnnouncements(serverUrl: String, tenant: String, jwtToken: String): String = withContext(Dispatchers.IO) {
        // This would call https://{serverUrl}/WebUntis/api/rest/view/v1/dashboard/cards/status
        // using the Bearer JWT token from the new authentication flow.
        println("WebUntisApi: fetchAnnouncements called for tenant $tenant")
        return@withContext "Keine neuen Ankündigungen."
    }

    suspend fun fetchHomeworkDetails(
        serverUrl: String,
        elementId: String,
        start: String,
        end: String,
        jwtToken: String,
        jsessionId: String
    ): String = withContext(Dispatchers.IO) {
        try {
            val url = "https://$serverUrl/WebUntis/api/rest/view/v2/calendar-entry/detail?elementId=$elementId&elementType=5&endDateTime=${end.encodeURLQueryComponent()}&homeworkOption=DUE&startDateTime=${start.encodeURLQueryComponent()}"
            val response = client.get(url) {
                header("authorization", "Bearer $jwtToken")
                header("cookie", "JSESSIONID=$jsessionId")
                header("accept", "application/json, text/plain, */*")
                header("priority", "u=1, i")
            }
            if (response.status.isSuccess()) {
                response.bodyAsText()
            } else {
                "Fehler beim Laden der Hausaufgaben: HTTP ${response.status.value}"
            }
        } catch (e: Exception) {
            "Ausnahme beim Laden der Hausaufgaben: ${e.message}"
        }
    }
}

