package com.example.runtime

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

data class RuntimeInfo(
    val name: String,
    val binaryName: String,
    val isInstalled: Boolean,
    val version: String,
    val location: String,
    val status: String,
    val testCommand: String
)

class RuntimeManager {

    private val targetRuntimes = listOf(
        Triple("Bash / Shell", "sh", "echo 'Bash Subsystem OK'"),
        Triple("Python", "python3", "python3 -c 'print(\"Python OK\")'"),
        Triple("Node.js", "node", "node -e 'console.log(\"Node OK\")'"),
        Triple("Java", "java", "java -version"),
        Triple("C / C++ (Clang/GCC)", "clang", "clang --version"),
        Triple("PHP", "php", "php -v"),
        Triple("Ruby", "ruby", "ruby -v"),
        Triple("Go", "go", "go version"),
        Triple("Rust", "rustc", "rustc --version")
    )

    suspend fun detectAllRuntimes(): List<RuntimeInfo> = withContext(Dispatchers.IO) {
        targetRuntimes.map { (name, binary, testCmd) ->
            probeRuntime(name, binary, testCmd)
        }
    }

    suspend fun probeRuntime(name: String, binary: String, testCmd: String): RuntimeInfo = withContext(Dispatchers.IO) {
        val path = findExecutablePath(binary)
        if (path != null) {
            val versionStr = getExecutableVersion(binary)
            RuntimeInfo(
                name = name,
                binaryName = binary,
                isInstalled = true,
                version = versionStr,
                location = path,
                status = "Ready & Operational",
                testCommand = testCmd
            )
        } else {
            RuntimeInfo(
                name = name,
                binaryName = binary,
                isInstalled = false,
                version = "Not detected",
                location = "N/A",
                status = "Unavailable in system PATH",
                testCommand = testCmd
            )
        }
    }

    private fun findExecutablePath(binary: String): String? {
        val searchDirs = listOf(
            "/system/bin",
            "/system/xbin",
            "/vendor/bin",
            "/apex/com.android.runtime/bin",
            "/data/local/tmp"
        )
        for (dir in searchDirs) {
            val file = File(dir, binary)
            if (file.exists() && file.canExecute()) {
                return file.absolutePath
            }
        }
        return try {
            val p = ProcessBuilder("/system/bin/sh", "-c", "which $binary").start()
            val reader = BufferedReader(InputStreamReader(p.inputStream))
            val line = reader.readLine()
            p.waitFor()
            if (p.exitValue() == 0 && !line.isNullOrBlank()) line.trim() else null
        } catch (_: Exception) {
            null
        }
    }

    private fun getExecutableVersion(binary: String): String {
        return try {
            val p = ProcessBuilder("/system/bin/sh", "-c", "$binary --version 2>&1 || $binary -v 2>&1").start()
            val reader = BufferedReader(InputStreamReader(p.inputStream))
            val line = reader.readLine()
            p.waitFor()
            line?.take(50)?.trim() ?: "Available"
        } catch (_: Exception) {
            "Available"
        }
    }

    suspend fun testExecution(runtime: RuntimeInfo): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        if (!runtime.isInstalled) {
            return@withContext false to "Cannot run test: Runtime is not installed on device."
        }
        try {
            val p = ProcessBuilder("/system/bin/sh", "-c", runtime.testCommand).start()
            val output = p.inputStream.bufferedReader().readText() + p.errorStream.bufferedReader().readText()
            p.waitFor()
            val ok = p.exitValue() == 0
            ok to if (ok) "✓ Test Succeeded:\n$output" else "✗ Test Failed:\n$output"
        } catch (e: Exception) {
            false to "Execution Error: ${e.message}"
        }
    }
}
