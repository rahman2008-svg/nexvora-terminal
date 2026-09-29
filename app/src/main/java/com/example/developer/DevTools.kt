package com.example.developer

import android.net.Uri
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLDecoder
import java.net.URLEncoder
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

object DevTools {

    // 1. JSON Formatter & Minifier
    fun formatJson(input: String, indentSpaces: Int = 2): Pair<Boolean, String> {
        val trimmed = input.trim()
        return try {
            if (trimmed.startsWith("{")) {
                val obj = JSONObject(trimmed)
                true to obj.toString(indentSpaces)
            } else if (trimmed.startsWith("[")) {
                val arr = JSONArray(trimmed)
                true to arr.toString(indentSpaces)
            } else {
                false to "Invalid JSON format: Must start with '{' or '['"
            }
        } catch (e: Exception) {
            false to "JSON Parse Error: ${e.message}"
        }
    }

    fun minifyJson(input: String): Pair<Boolean, String> {
        val trimmed = input.trim()
        return try {
            if (trimmed.startsWith("{")) {
                true to JSONObject(trimmed).toString()
            } else if (trimmed.startsWith("[")) {
                true to JSONArray(trimmed).toString()
            } else {
                false to "Invalid JSON format"
            }
        } catch (e: Exception) {
            false to "JSON Error: ${e.message}"
        }
    }

    // 2. Base64
    fun encodeBase64(input: String): String {
        return Base64.encodeToString(input.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
    }

    fun decodeBase64(input: String): Pair<Boolean, String> {
        return try {
            val bytes = Base64.decode(input, Base64.DEFAULT)
            true to String(bytes, Charsets.UTF_8)
        } catch (e: Exception) {
            false to "Invalid Base64: ${e.message}"
        }
    }

    // 3. URL Encoder / Decoder
    fun encodeUrl(input: String): String = URLEncoder.encode(input, "UTF-8")
    fun decodeUrl(input: String): Pair<Boolean, String> {
        return try {
            true to URLDecoder.decode(input, "UTF-8")
        } catch (e: Exception) {
            false to "Invalid URL string: ${e.message}"
        }
    }

    // 4. Regex Tester
    data class RegexMatch(val text: String, val range: IntRange)
    fun testRegex(patternStr: String, target: String): Pair<Boolean, List<RegexMatch>> {
        return try {
            val regex = Regex(patternStr)
            val matches = regex.findAll(target).map {
                RegexMatch(it.value, it.range)
            }.toList()
            true to matches
        } catch (_: Exception) {
            false to emptyList()
        }
    }

    // 5. UUID Generator
    fun generateUuids(count: Int = 1): List<String> {
        return (1..count.coerceIn(1, 20)).map { UUID.randomUUID().toString() }
    }

    // 6. Hash Generator
    fun calculateHash(input: String, algorithm: String = "SHA-256"): String {
        val md = MessageDigest.getInstance(algorithm)
        val digest = md.digest(input.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    // 7. Timestamp Converter
    fun epochToFormatted(epochSec: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.getDefault())
        return sdf.format(Date(epochSec * 1000L))
    }

    // 8. Text Formatter (Case conversions)
    fun toCamelCase(input: String): String {
        val words = input.split(Regex("[^a-zA-Z0-9]+")).filter { it.isNotBlank() }
        if (words.isEmpty()) return ""
        return words[0].lowercase() + words.drop(1).joinToString("") { it.capitalize(Locale.getDefault()) }
    }

    fun toSnakeCase(input: String): String {
        return input.trim()
            .replace(Regex("([a-z])([A-Z])"), "$1_$2")
            .replace(Regex("[^a-zA-Z0-9]+"), "_")
            .lowercase()
    }

    fun toKebabCase(input: String): String {
        return toSnakeCase(input).replace("_", "-")
    }

    // 9. HTTP Request Tester
    data class HttpResponse(
        val statusCode: Int,
        val statusMessage: String,
        val headers: Map<String, String>,
        val body: String,
        val latencyMs: Long
    )

    suspend fun executeHttpRequest(
        urlStr: String,
        method: String = "GET",
        headers: Map<String, String> = emptyMap(),
        requestBody: String = ""
    ): Result<HttpResponse> = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        var connection: HttpURLConnection? = null
        try {
            val url = URL(urlStr)
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = method
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            headers.forEach { (k, v) -> connection.setRequestProperty(k, v) }

            if ((method == "POST" || method == "PUT" || method == "PATCH") && requestBody.isNotEmpty()) {
                connection.doOutput = true
                connection.outputStream.use { it.write(requestBody.toByteArray(Charsets.UTF_8)) }
            }

            val statusCode = connection.responseCode
            val statusMessage = connection.responseMessage ?: ""
            val stream = if (statusCode in 200..399) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader()?.use { it.readText() } ?: ""

            val respHeaders = mutableMapOf<String, String>()
            connection.headerFields.forEach { (k, v) ->
                if (k != null) respHeaders[k] = v.joinToString(", ")
            }

            val latency = System.currentTimeMillis() - startTime
            Result.success(
                HttpResponse(
                    statusCode = statusCode,
                    statusMessage = statusMessage,
                    headers = respHeaders,
                    body = body,
                    latencyMs = latency
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            connection?.disconnect()
        }
    }

    // 10. Environment Variables Viewer
    fun getEnvironmentVariables(): Map<String, String> {
        val env = System.getenv().toMutableMap()
        env["USER"] = env["USER"] ?: "nexvora"
        env["HOME"] = env["HOME"] ?: "/data/user/0/com.aistudio.nexvoraterminal.vxrt/files/NexVora"
        env["TERM"] = "xterm-256color"
        env["NEXVORA_PLATFORM"] = "Android-Subsystem"
        return env.toSortedMap()
    }
}
