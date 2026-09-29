package com.example.terminal

import com.example.core.NexCommandDispatcher
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.UUID

data class TerminalLine(
    val id: String = UUID.randomUUID().toString(),
    val rawText: String,
    val isCommand: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

class TerminalSession(
    val id: String = UUID.randomUUID().toString(),
    var title: String = "Terminal 1",
    var workingDir: File,
    private val dispatcher: NexCommandDispatcher,
    private val maxHistory: Int = 2000
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _lines = MutableStateFlow<List<TerminalLine>>(emptyList())
    val lines: StateFlow<List<TerminalLine>> = _lines.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val commandHistoryList = mutableListOf<String>()
    private var historyIndex = -1

    private var activeProcess: Process? = null

    init {
        appendOutput("\u001B[1;36m====================================================\u001B[0m")
        appendOutput("\u001B[1;36m  ⚡ NexVora Terminal v1.0.0 [Android Subsystem]\u001B[0m")
        appendOutput("\u001B[1;35m  Code • Run • Build • Anywhere\u001B[0m")
        appendOutput("\u001B[33m  Type '\u001B[1;32mnex help\u001B[0m\u001B[33m' for command palette or '\u001B[1;32mnex doctor\u001B[0m\u001B[33m' for diagnostics.\u001B[0m")
        appendOutput("\u001B[1;36m====================================================\u001B[0m")
        appendOutput("\u001B[90mSession started at ${workingDir.path}\u001B[0m\n")
    }

    fun appendOutput(text: String, isCommand: Boolean = false) {
        val newLines = text.split("\n").map { line ->
            TerminalLine(rawText = line, isCommand = isCommand)
        }
        val current = _lines.value.toMutableList()
        current.addAll(newLines)
        if (current.size > maxHistory) {
            _lines.value = current.takeLast(maxHistory)
        } else {
            _lines.value = current
        }
    }

    fun clear() {
        _lines.value = emptyList()
    }

    fun getPreviousCommand(): String? {
        if (commandHistoryList.isEmpty()) return null
        if (historyIndex < commandHistoryList.size - 1) {
            historyIndex++
            return commandHistoryList[commandHistoryList.size - 1 - historyIndex]
        }
        return commandHistoryList.firstOrNull()
    }

    fun getNextCommand(): String? {
        if (commandHistoryList.isEmpty() || historyIndex <= 0) {
            historyIndex = -1
            return ""
        }
        historyIndex--
        return commandHistoryList[commandHistoryList.size - 1 - historyIndex]
    }

    fun execute(input: String) {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return

        commandHistoryList.add(trimmed)
        historyIndex = -1

        val promptStr = "\u001B[1;36mnexvora@android\u001B[0m:\u001B[1;34m~/${workingDir.name}\u001B[0m$ $trimmed"
        appendOutput(promptStr, isCommand = true)

        // Built-in `clear`
        if (trimmed == "clear" || trimmed == "cls") {
            clear()
            return
        }

        // Built-in `cd`
        if (trimmed.startsWith("cd ") || trimmed == "cd") {
            handleCd(trimmed)
            return
        }

        scope.launch {
            _isRunning.value = true
            try {
                // Intercept custom `nex` commands
                if (trimmed.startsWith("nex ") || trimmed == "nex") {
                    val result = dispatcher.dispatch(trimmed, workingDir, this@TerminalSession)
                    if (result.isNotBlank()) {
                        appendOutput(result)
                    }
                    return@launch
                }

                // Execute via Android shell process
                executeShellCommand(trimmed)
            } catch (e: Exception) {
                appendOutput("\u001B[1;31mError: ${e.localizedMessage ?: "Unknown execution error"}\u001B[0m")
            } finally {
                _isRunning.value = false
            }
        }
    }

    private fun handleCd(cmd: String) {
        val target = cmd.removePrefix("cd").trim()
        val newDir = when {
            target.isEmpty() || target == "~" -> dispatcher.workspaceManager.rootDir
            target == ".." -> workingDir.parentFile ?: workingDir
            target.startsWith("/") -> File(target)
            else -> File(workingDir, target)
        }

        if (newDir.exists() && newDir.isDirectory) {
            workingDir = newDir
        } else {
            appendOutput("\u001B[31mcd: no such file or directory: $target\u001B[0m")
        }
    }

    private suspend fun executeShellCommand(cmd: String) = withContext(Dispatchers.IO) {
        try {
            val shell = if (File("/system/bin/sh").exists()) "/system/bin/sh" else "sh"
            val pb = ProcessBuilder(shell, "-c", cmd)
            pb.directory(workingDir)
            val env = pb.environment()
            env["TERM"] = "xterm-256color"
            env["HOME"] = dispatcher.workspaceManager.rootDir.absolutePath
            env["PATH"] = "${dispatcher.workspaceManager.packagesDir.absolutePath}/bin:/system/bin:/system/xbin:/vendor/bin"
            env["NEXVORA_WORKSPACE"] = dispatcher.workspaceManager.rootDir.absolutePath

            pb.redirectErrorStream(true)
            val process = pb.start()
            activeProcess = process

            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                line?.let { appendOutput(it) }
            }
            process.waitFor()
            val exitCode = process.exitValue()
            if (exitCode != 0) {
                appendOutput("\u001B[90m[Process finished with exit code $exitCode]\u001B[0m")
            }
        } catch (e: Exception) {
            appendOutput("\u001B[31mProcess failure: ${e.message}\u001B[0m")
        } finally {
            activeProcess = null
        }
    }

    fun interrupt() {
        try {
            activeProcess?.destroy()
            appendOutput("\u001B[33m^C [Process terminated]\u001B[0m")
        } catch (_: Exception) {}
        _isRunning.value = false
    }

    fun destroy() {
        interrupt()
        scope.cancel()
    }
}
