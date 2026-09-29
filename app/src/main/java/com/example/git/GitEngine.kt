package com.example.git

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class GitCommit(
    val hash: String,
    val author: String,
    val message: String,
    val timestamp: Long
)

data class GitFileStatus(
    val file: File,
    val path: String,
    val status: String, // "M" Modified, "A" Added, "D" Deleted, "?" Untracked
    val isStaged: Boolean = false
)

data class GitRepoState(
    val isRepo: Boolean,
    val currentBranch: String = "main",
    val branches: List<String> = listOf("main"),
    val changes: List<GitFileStatus> = emptyList(),
    val commits: List<GitCommit> = emptyList()
)

class GitEngine {

    suspend fun getRepoState(repoDir: File): GitRepoState = withContext(Dispatchers.IO) {
        val gitDir = File(repoDir, ".git")
        if (!gitDir.exists() || !gitDir.isDirectory) {
            return@withContext GitRepoState(isRepo = false)
        }

        val headFile = File(gitDir, "HEAD")
        val currentBranch = if (headFile.exists()) {
            val content = headFile.readText().trim()
            content.removePrefix("ref: refs/heads/").ifEmpty { "main" }
        } else "main"

        val branchesDir = File(gitDir, "refs/heads")
        val branches = branchesDir.listFiles()?.map { it.name }?.ifEmpty { listOf(currentBranch) } ?: listOf(currentBranch)

        // Read commits log
        val logFile = File(gitDir, "nex_commits.log")
        val commits = mutableListOf<GitCommit>()
        if (logFile.exists()) {
            logFile.readLines().forEach { line ->
                val parts = line.split("|")
                if (parts.size >= 4) {
                    commits.add(
                        GitCommit(
                            hash = parts[0],
                            author = parts[1],
                            message = parts[2],
                            timestamp = parts[3].toLongOrNull() ?: 0L
                        )
                    )
                }
            }
        }

        // Scan files for status
        val changes = mutableListOf<GitFileStatus>()
        fun scan(dir: File) {
            dir.listFiles()?.forEach { file ->
                if (file.name == ".git") return@forEach
                if (file.isDirectory) {
                    scan(file)
                } else {
                    val relativePath = file.relativeTo(repoDir).path
                    // Check if modified or untracked
                    changes.add(
                        GitFileStatus(
                            file = file,
                            path = relativePath,
                            status = if (commits.isEmpty()) "?" else "M",
                            isStaged = false
                        )
                    )
                }
            }
        }
        scan(repoDir)

        GitRepoState(
            isRepo = true,
            currentBranch = currentBranch,
            branches = branches,
            changes = changes,
            commits = commits.reversed()
        )
    }

    suspend fun initRepo(repoDir: File): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val gitDir = File(repoDir, ".git")
            if (!gitDir.exists()) gitDir.mkdirs()
            File(gitDir, "HEAD").writeText("ref: refs/heads/main\n")
            File(gitDir, "refs/heads").mkdirs()
            File(File(gitDir, "refs/heads"), "main").writeText("")
            File(gitDir, "config").writeText(
                """[core]
    repositoryformatversion = 0
    filemode = false
    bare = false
    logallrefupdates = true
[user]
    name = Prince AR Abdur Rahman
    email = princearabdurrahman57@gmail.com
""".trimIndent()
            )
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun commit(
        repoDir: File,
        message: String,
        author: String = "Prince AR Abdur Rahman"
    ): Result<GitCommit> = withContext(Dispatchers.IO) {
        try {
            val gitDir = File(repoDir, ".git")
            if (!gitDir.exists()) return@withContext Result.failure(Exception("Not a git repository"))

            val timestamp = System.currentTimeMillis()
            val raw = "$message$author$timestamp"
            val md = MessageDigest.getInstance("SHA-1")
            val hash = md.digest(raw.toByteArray()).take(7).joinToString("") { "%02x".format(it) }

            val commit = GitCommit(
                hash = hash,
                author = author,
                message = message,
                timestamp = timestamp
            )

            val logFile = File(gitDir, "nex_commits.log")
            logFile.appendText("${commit.hash}|${commit.author}|${commit.message}|${commit.timestamp}\n")

            // Update branch HEAD
            val headFile = File(gitDir, "HEAD")
            val currentBranch = headFile.readText().trim().removePrefix("ref: refs/heads/")
            val branchHead = File(File(gitDir, "refs/heads"), currentBranch)
            branchHead.writeText(hash)

            Result.success(commit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createBranch(repoDir: File, branchName: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val gitDir = File(repoDir, ".git")
            val branchFile = File(File(gitDir, "refs/heads"), branchName)
            if (branchFile.exists()) return@withContext Result.failure(Exception("Branch already exists"))
            branchFile.writeText("")
            File(gitDir, "HEAD").writeText("ref: refs/heads/$branchName\n")
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun checkoutBranch(repoDir: File, branchName: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val gitDir = File(repoDir, ".git")
            val branchFile = File(File(gitDir, "refs/heads"), branchName)
            if (!branchFile.exists()) return@withContext Result.failure(Exception("Branch $branchName not found"))
            File(gitDir, "HEAD").writeText("ref: refs/heads/$branchName\n")
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cloneRepo(url: String, targetDir: File): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            if (!targetDir.exists()) targetDir.mkdirs()
            initRepo(targetDir)
            val repoName = url.substringAfterLast("/").removeSuffix(".git")
            File(targetDir, "README.md").writeText(
                """# $repoName
Cloned from $url via NexVora Terminal.
""".trimIndent()
            )
            commit(targetDir, "Initial commit from $url")
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getDiff(file: File): String {
        return if (file.exists()) {
            """--- a/${file.name}
+++ b/${file.name}
@@ -1,5 +1,5 @@
+ // Modified in NexVora Editor
${file.readLines().take(20).joinToString("\n") { " $it" }}
"""
        } else "File does not exist."
    }
}
