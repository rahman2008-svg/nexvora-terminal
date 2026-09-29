package com.example.core

import android.content.Context
import java.io.File

class WorkspaceManager(private val context: Context) {

    val rootDir: File by lazy {
        val dir = File(context.filesDir, "NexVora")
        if (!dir.exists()) dir.mkdirs()
        dir
    }

    val projectsDir: File by lazy {
        File(rootDir, "projects").apply { if (!exists()) mkdirs() }
    }

    val downloadsDir: File by lazy {
        File(rootDir, "downloads").apply { if (!exists()) mkdirs() }
    }

    val scriptsDir: File by lazy {
        File(rootDir, "scripts").apply { if (!exists()) mkdirs() }
    }

    val packagesDir: File by lazy {
        File(rootDir, "packages").apply { if (!exists()) mkdirs() }
    }

    val workspaceDir: File by lazy {
        File(rootDir, "workspace").apply { if (!exists()) mkdirs() }
    }

    fun initializeWorkspace(onProjectCreated: (name: String, path: String, type: String) -> Unit = { _, _, _ -> }) {
        projectsDir
        downloadsDir
        scriptsDir
        packagesDir
        workspaceDir

        // Create Demo Python Project if not exists
        val demoPythonDir = File(projectsDir, "demo-python")
        if (!demoPythonDir.exists()) {
            demoPythonDir.mkdirs()
            File(demoPythonDir, "main.py").writeText(
                """# NexVora Terminal - Python Demo
# Code • Run • Build • Anywhere

import sys
import datetime

def main():
    print("=" * 45)
    print("  🚀 Welcome to NexVora Terminal!")
    print("  Developer: Prince AR Abdur Rahman")
    print("  Publisher: NexVora Lab's Ofc")
    print("=" * 45)
    print(f"Timestamp: {datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    print(f"Python Platform: {sys.platform}")
    print("-" * 45)
    
    tasks = ["Code in Editor", "Run in Terminal", "Manage with Git", "Build Project"]
    for i, task in enumerate(tasks, 1):
        print(f" [{i}] Status: Ready -> {task}")
    
    print("-" * 45)
    print("Execution finished successfully.")

if __name__ == "__main__":
    main()
""".trimIndent()
            )
            File(demoPythonDir, "README.md").writeText(
                """# Demo Python Project
Welcome to your first NexVora project!

### How to Run:
1. Open this project in the Code Editor.
2. Go to the Build or Terminal tab.
3. In Terminal run:
   `python main.py` or `nex run main.py`
""".trimIndent()
            )
            onProjectCreated("demo-python", demoPythonDir.absolutePath, "python")
        }

        // Create Demo Web Project if not exists
        val demoWebDir = File(projectsDir, "demo-web")
        if (!demoWebDir.exists()) {
            demoWebDir.mkdirs()
            File(demoWebDir, "index.html").writeText(
                """<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>NexVora Web App</title>
  <link rel="stylesheet" href="style.css">
</head>
<body>
  <div class="card">
    <div class="badge">NEXVORA TERMINAL</div>
    <h1>Code • Run • Build • Anywhere</h1>
    <p>Mobile developer workstation powered by Android.</p>
    <button id="actionBtn" onclick="handleClick()">Run Local Diagnostic</button>
    <div id="output" class="terminal-box">Ready.</div>
  </div>
  <script src="script.js"></script>
</body>
</html>
""".trimIndent()
            )
            File(demoWebDir, "style.css").writeText(
                """* { box-sizing: border-box; margin: 0; padding: 0; font-family: monospace, sans-serif; }
body { background: #0A0E17; color: #F8FAFC; display: flex; align-items: center; justify-content: center; min-height: 100vh; padding: 20px; }
.card { background: #111827; border: 1px solid #1E293B; border-radius: 16px; padding: 32px; max-width: 450px; text-align: center; box-shadow: 0 10px 30px rgba(0,229,255,0.1); }
.badge { display: inline-block; background: rgba(0,229,255,0.15); color: #00E5FF; padding: 6px 14px; border-radius: 20px; font-size: 11px; font-weight: bold; letter-spacing: 1px; margin-bottom: 16px; }
h1 { font-size: 20px; margin-bottom: 12px; color: #FFFFFF; }
p { font-size: 13px; color: #94A3B8; margin-bottom: 24px; }
button { background: #00E5FF; color: #0A0E17; border: none; padding: 12px 24px; border-radius: 8px; font-size: 14px; font-weight: bold; cursor: pointer; }
.terminal-box { margin-top: 20px; background: #05070B; border: 1px solid #334155; border-radius: 8px; padding: 12px; font-size: 12px; text-align: left; color: #10B981; }
""".trimIndent()
            )
            File(demoWebDir, "script.js").writeText(
                """function handleClick() {
  const output = document.getElementById('output');
  output.innerText = '✓ Local Web Server Active\n✓ Host: 127.0.0.1\n✓ NexVora Status: Optimal';
}
""".trimIndent()
            )
            File(demoWebDir, "README.md").writeText(
                """# Demo Web Project
Start the local HTTP server under **More -> Local Server** to preview this app!
""".trimIndent()
            )
            onProjectCreated("demo-web", demoWebDir.absolutePath, "web")
        }

        // Create Demo Script
        val scriptFile = File(scriptsDir, "system_info.sh")
        if (!scriptFile.exists()) {
            scriptFile.writeText(
                """#!/system/bin/sh
echo "=== NexVora System Diagnostic ==="
echo "Kernel: $(uname -a 2>/dev/null || echo 'Linux-Android')"
echo "Host Date: $(date)"
echo "Current Dir: $(pwd)"
echo "Storage Root: $rootDir"
echo "Status: System operational."
""".trimIndent()
            )
        }
    }

    fun getStorageStats(): StorageStats {
        val totalBytes = rootDir.totalSpace
        val freeBytes = rootDir.freeSpace
        val usedWorkspaceBytes = getFolderSize(rootDir)
        return StorageStats(
            totalBytes = totalBytes,
            freeBytes = freeBytes,
            workspaceBytes = usedWorkspaceBytes
        )
    }

    private fun getFolderSize(dir: File): Long {
        var size: Long = 0
        val files = dir.listFiles() ?: return 0L
        for (file in files) {
            size += if (file.isDirectory) getFolderSize(file) else file.length()
        }
        return size
    }
}

data class StorageStats(
    val totalBytes: Long,
    val freeBytes: Long,
    val workspaceBytes: Long
) {
    fun formatBytes(bytes: Long): String {
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0
        return when {
            gb >= 1.0 -> String.format("%.2f GB", gb)
            mb >= 1.0 -> String.format("%.2f MB", mb)
            kb >= 1.0 -> String.format("%.1f KB", kb)
            else -> "$bytes B"
        }
    }
}
