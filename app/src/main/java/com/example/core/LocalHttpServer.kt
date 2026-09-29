package com.example.core

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.*
import java.net.ServerSocket
import java.net.Socket
import java.net.URLConnection
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LocalHttpServer {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var serverSocket: ServerSocket? = null
    private var isRunning = false

    private val _status = MutableStateFlow(ServerStatus.STOPPED)
    val status: StateFlow<ServerStatus> = _status.asStateFlow()

    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs: StateFlow<List<String>> = _logs.asStateFlow()

    var host: String = "127.0.0.1"
        private set
    var port: Int = 8080
        private set
    var rootDir: File? = null
        private set

    enum class ServerStatus {
        STOPPED,
        STARTING,
        RUNNING,
        ERROR
    }

    fun start(dir: File, selectedPort: Int = 8080) {
        if (isRunning) stop()

        rootDir = dir
        port = selectedPort
        _status.value = ServerStatus.STARTING
        appendLog("Starting local web server on port $port serving: ${dir.name}...")

        scope.launch {
            try {
                serverSocket = ServerSocket(port)
                isRunning = true
                _status.value = ServerStatus.RUNNING
                appendLog("✓ Server active at http://$host:$port/")
                appendLog("Ready to accept incoming HTTP requests.")

                while (isRunning && !serverSocket!!.isClosed) {
                    val client = try {
                        serverSocket?.accept() ?: break
                    } catch (e: Exception) {
                        break
                    }
                    launch { handleClient(client) }
                }
            } catch (e: Exception) {
                _status.value = ServerStatus.ERROR
                appendLog("✗ Error starting server on port $port: ${e.message}")
            } finally {
                isRunning = false
                if (_status.value != ServerStatus.ERROR) {
                    _status.value = ServerStatus.STOPPED
                }
            }
        }
    }

    fun stop() {
        if (!isRunning) return
        isRunning = false
        try {
            serverSocket?.close()
        } catch (_: Exception) {}
        serverSocket = null
        _status.value = ServerStatus.STOPPED
        appendLog("Server stopped.")
    }

    fun restart(dir: File, selectedPort: Int = port) {
        stop()
        start(dir, selectedPort)
    }

    private fun handleClient(socket: Socket) {
        try {
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
            val rawRequest = reader.readLine() ?: return
            val tokens = rawRequest.split(" ")
            if (tokens.size < 2) return

            val method = tokens[0]
            var path = tokens[1]
            if (path == "/" || path.isEmpty()) {
                path = "/index.html"
            }

            val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            appendLog("[$timestamp] $method $path from ${socket.inetAddress.hostAddress}")

            val targetFile = File(rootDir, path.removePrefix("/"))
            val out = socket.getOutputStream()

            if (targetFile.exists() && !targetFile.isDirectory) {
                val mimeType = getMimeType(targetFile)
                val bytes = targetFile.readBytes()

                val header = "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: $mimeType\r\n" +
                        "Content-Length: ${bytes.size}\r\n" +
                        "Connection: close\r\n\r\n"

                out.write(header.toByteArray())
                out.write(bytes)
            } else {
                val body = "<html><body style='background:#0A0E17;color:white;font-family:monospace;padding:24px;'><h2>404 Not Found</h2><p>File $path does not exist in ${rootDir?.name}</p></body></html>"
                val header = "HTTP/1.1 404 Not Found\r\n" +
                        "Content-Type: text/html\r\n" +
                        "Content-Length: ${body.length}\r\n" +
                        "Connection: close\r\n\r\n"
                out.write(header.toByteArray())
                out.write(body.toByteArray())
            }
            out.flush()
        } catch (e: Exception) {
            appendLog("Client request handling error: ${e.message}")
        } finally {
            try { socket.close() } catch (_: Exception) {}
        }
    }

    private fun getMimeType(file: File): String {
        return when (file.extension.lowercase()) {
            "html", "htm" -> "text/html; charset=UTF-8"
            "css" -> "text/css"
            "js" -> "application/javascript"
            "json" -> "application/json"
            "png" -> "image/png"
            "jpg", "jpeg" -> "image/jpeg"
            "svg" -> "image/svg+xml"
            "txt" -> "text/plain"
            else -> URLConnection.guessContentTypeFromName(file.name) ?: "application/octet-stream"
        }
    }

    private fun appendLog(msg: String) {
        val current = _logs.value.toMutableList()
        current.add(msg)
        if (current.size > 200) {
            _logs.value = current.takeLast(200)
        } else {
            _logs.value = current
        }
    }

    fun clearLogs() {
        _logs.value = emptyList()
    }
}
