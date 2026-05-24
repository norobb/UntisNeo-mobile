package com.example.data.api

import android.util.Log
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar

class WebUntisApi {
    private val cookieJar = object : CookieJar {
        private val cookies = mutableListOf<Cookie>()
        override fun saveFromResponse(url: HttpUrl, newCookies: List<Cookie>) {
            if (newCookies.isNotEmpty()) {
                val newNames = newCookies.map { it.name }.toSet()
                synchronized(cookies) {
                    cookies.removeAll { old -> newNames.contains(old.name) }
                    cookies.addAll(newCookies)
                }
            }
        }

        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            val now = System.currentTimeMillis()
            synchronized(cookies) {
                cookies.removeAll { it.expiresAt < now }
                return cookies.filter { it.matches(url) }
            }
        }
    }

    private val clientAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

    private val client = OkHttpClient.Builder()
        .cookieJar(cookieJar)
        .addInterceptor { chain ->
            val original = chain.request()
            val request = original.newBuilder()
                .header("User-Agent", clientAgent)
                .method(original.method, original.body)
                .build()
            chain.proceed(request)
        }
        .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun fetchTimetable(
        serverUrl: String, // e.g. "hh5886.webuntis.com"
        school: String, // e.g. "hh5886"
        user: String,
        pass: String
    ): List<com.example.data.room.TimetableLesson>? = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            var cleanServerUrl = serverUrl.trim()
            if (cleanServerUrl.startsWith("http://") || cleanServerUrl.startsWith("https://")) {
                try {
                    val uri = java.net.URI(cleanServerUrl)
                    val host = uri.host
                    if (host != null && host.isNotEmpty()) {
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
            Log.d("WebUntisApi", "Starting sync. Server: $cleanServerUrl, School: $school")
            val schoolEnc = java.net.URLEncoder.encode(school, "UTF-8").replace("+", "%20")
            val baseUrl = "https://$cleanServerUrl/WebUntis/jsonrpc.do?school=$schoolEnc"

            // 1. Authenticate
            val authReq = JSONObject().apply {
                put("id", "auth")
                put("method", "authenticate")
                put("params", JSONObject().apply {
                    put("user", user)
                    put("password", pass)
                    put("client", "WebUntis") // Using default client name to ensure compatibility
                })
                put("jsonrpc", "2.0")
            }

            val authRequest = Request.Builder()
                .url(baseUrl)
                .post(authReq.toString().toRequestBody(jsonMediaType))
                .build()

            val authResponse = client.newCall(authRequest).execute()
            if (!authResponse.isSuccessful) {
                throw Exception("HTTP-Fehler ${authResponse.code} bei der Anmeldung")
            }
            val authBody = authResponse.body?.string() ?: throw Exception("Leere Antwort bei der Anmeldung")
            val authJson = JSONObject(authBody)
            if (authJson.has("error")) {
                val errorObj = authJson.optJSONObject("error")
                val errMsg = errorObj?.optString("message") ?: "Unbekannter Fehler"
                if (errMsg.lowercase().contains("bad credentials") || errMsg.lowercase().contains("invalid")) {
                    throw Exception("Falsche Zugangsdaten (Benutzername, Passwort oder Schule)")
                } else {
                    throw Exception("WebUntis Server-Fehler: $errMsg")
                }
            }
            
            val authResult = authJson.optJSONObject("result") ?: throw Exception("Fehlerhafte Antwort vom WebUntis Server erhalten")
            val personType = authResult.optInt("personType", 0)
            val personId = authResult.optInt("personId", 0)

            // 2. Fetch dependencies (Subjects, Rooms, Teachers)
            val subjectsMap = fetchMap(baseUrl, "getSubjects")
            val roomsMap = fetchMap(baseUrl, "getRooms")
            val teachersMap = fetchMap(baseUrl, "getTeachers")

            // 3. Fetch Timetable for exactly 31 days starting from Monday of last week
            val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
            val cal = Calendar.getInstance()
            cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            cal.add(Calendar.DAY_OF_YEAR, -7) // Start from Monday of last week
            val startDate = sdf.format(cal.time).toInt()
            
            cal.add(Calendar.DAY_OF_YEAR, 30) // Add 30 days for exactly 31 days range
            val endDate = sdf.format(cal.time).toInt()

            Log.d("WebUntisApi", "Fetching timetable from $startDate to $endDate")

            val ttReq = JSONObject().apply {
                put("id", "tt")
                put("method", "getTimetable")
                put("params", JSONObject().apply {
                    put("options", JSONObject().apply {
                        put("element", JSONObject().apply {
                            put("id", personId)
                            put("type", personType)
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

            val ttRequest = Request.Builder()
                .url(baseUrl)
                .post(ttReq.toString().toRequestBody(jsonMediaType))
                .build()

            val ttResponse = client.newCall(ttRequest).execute()
            if (!ttResponse.isSuccessful) {
                throw Exception("HTTP-Fehler ${ttResponse.code} bei der Stundenplan-Abfrage")
            }
            val ttBody = ttResponse.body?.string() ?: throw Exception("Leere Antwort bei der Stundenplan-Abfrage")
            val ttJson = JSONObject(ttBody)
            
            if (ttJson.has("error")) {
                val errorObj = ttJson.optJSONObject("error")
                val errMsg = errorObj?.optString("message") ?: "Unbekannter Server-Fehler"
                throw Exception("Fehler bei Stundenplan-Abfrage: $errMsg")
            }
            
            val ttArray = ttJson.getJSONArray("result")
            val lessons = mutableListOf<com.example.data.room.TimetableLesson>()
            
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            for (i in 0 until ttArray.length()) {
                val item = ttArray.getJSONObject(i)
                val idStr = item.getInt("id").toString()
                
                val dateInt = item.getInt("date")
                val dYear = dateInt / 10000
                val dMonth = (dateInt / 100) % 100
                val dDay = dateInt % 100
                val dateCal = Calendar.getInstance().apply { set(dYear, dMonth - 1, dDay) }
                val dateStr = dateFormat.format(dateCal.time)
                
                val dayFormat = SimpleDateFormat("EE", Locale.ENGLISH)
                val dayOfWeekStr = dayFormat.format(dateCal.time).take(2)
                
                val startInt = item.getInt("startTime")
                val endInt = item.getInt("endTime")
                val startTimeStr = formatTime(startInt)
                val endTimeStr = formatTime(endInt)
                
                // Get subject, room, teacher
                var subjectStr = ""
                var subjectCodeStr = ""
                var roomStr = ""
                var teacherNameStr = ""
                var teacherCodeStr = ""
                
                if (item.has("su") && item.getJSONArray("su").length() > 0) {
                    val suId = item.getJSONArray("su").getJSONObject(0).getInt("id")
                    val suObj = subjectsMap[suId]
                    if (suObj != null) {
                        subjectStr = suObj.optString("longName", "")
                        subjectCodeStr = suObj.optString("name", "")
                    }
                }
                
                if (item.has("ro") && item.getJSONArray("ro").length() > 0) {
                    val roId = item.getJSONArray("ro").getJSONObject(0).getInt("id")
                    val roObj = roomsMap[roId]
                    if (roObj != null) {
                        roomStr = roObj.optString("name", "")
                    }
                }
                
                if (item.has("te") && item.getJSONArray("te").length() > 0) {
                    val teId = item.getJSONArray("te").getJSONObject(0).getInt("id")
                    val teObj = teachersMap[teId]
                    if (teObj != null) {
                        teacherNameStr = teObj.optString("longName", "")
                        teacherCodeStr = teObj.optString("name", "")
                    }
                }
                
                var status = "NORMAL"
                if (item.has("code") && item.getString("code") == "cancelled") {
                    status = "CANCELLED"
                } else if (item.has("code") && item.getString("code") == "irregular") {
                    status = "SUBSTITUTION"
                }
                
                var info = ""
                if (item.has("lstext")) info += item.getString("lstext") + " "
                if (item.has("substText")) info += item.getString("substText") + " "
                if (item.has("info")) info += item.getString("info")
                
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
            val logoutReq = JSONObject().apply {
                put("id", "logout")
                put("method", "logout")
                put("params", JSONObject())
                put("jsonrpc", "2.0")
            }
            client.newCall(Request.Builder().url(baseUrl).post(logoutReq.toString().toRequestBody(jsonMediaType)).build()).execute()
            
            Log.d("WebUntisApi", "Fetched ${lessons.size} lessons.")
            return@withContext lessons

        } catch (e: Exception) {
            Log.e("WebUntisApi", "Exception in API: ${e.localizedMessage}", e)
            val friendlyMsg = when {
                e.message?.startsWith("HTTP-Fehler") == true || e.message?.startsWith("WebUntis") == true || e.message?.startsWith("Falsche Zugangsdaten") == true || e.message?.startsWith("Fehler bei Stundenplan-Abfrage") == true -> e.message!!
                e is java.net.UnknownHostException || e.localizedMessage?.contains("Unable to resolve host") == true -> "Der Server ist nicht erreichbar. Bitte überprüfe die Server-Adresse und deine Internetverbindung."
                e is java.net.SocketTimeoutException || e.localizedMessage?.contains("timeout") == true -> "Verbindung zeitüberschritten. Der WebUntis-Server antwortet zurzeit nicht."
                e is java.io.IOException -> "Netzwerkfehler: ${e.localizedMessage ?: "Verbindung abgebrochen"}"
                else -> "Fehler: ${e.localizedMessage ?: "Unbekannter Fehler bei der Synchronisation"}"
            }
            throw Exception(friendlyMsg, e)
        }
    }

    private fun fetchMap(baseUrl: String, method: String): Map<Int, JSONObject> {
        return try {
            val req = JSONObject().apply {
                put("id", method)
                put("method", method)
                put("params", JSONObject())
                put("jsonrpc", "2.0")
            }
            val response = client.newCall(Request.Builder().url(baseUrl).post(req.toString().toRequestBody(jsonMediaType)).build()).execute()
            val body = response.body?.string() ?: return emptyMap()
            val json = JSONObject(body)
            if (json.has("error")) return emptyMap()
            val arr = json.getJSONArray("result")
            val map = mutableMapOf<Int, JSONObject>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                map[obj.getInt("id")] = obj
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
        return String.format(Locale.US, "%02d:%02d", h, m)
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
}
