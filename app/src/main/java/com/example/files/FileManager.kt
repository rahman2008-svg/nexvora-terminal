package com.example.files

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest

data class FileItem(
    val file: File,
    val name: String,
    val isDirectory: Boolean,
    val size: Long,
    val lastModified: Long,
    val extension: String,
    val isHidden: Boolean = false
)

enum class FileSortOption {
    NAME_ASC,
    NAME_DESC,
    DATE_DESC,
    DATE_ASC,
    SIZE_DESC,
    SIZE_ASC
}

class FileManager {

    fun listFiles(dir: File, sortOption: FileSortOption = FileSortOption.NAME_ASC): List<FileItem> {
        val files = dir.listFiles() ?: return emptyList()
        val items = files.map { file ->
            FileItem(
                file = file,
                name = file.name,
                isDirectory = file.isDirectory,
                size = if (file.isDirectory) (file.listFiles()?.size?.toLong() ?: 0L) else file.length(),
                lastModified = file.lastModified(),
                extension = file.extension,
                isHidden = file.name.startsWith(".")
            )
        }

        return when (sortOption) {
            FileSortOption.NAME_ASC -> items.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
            FileSortOption.NAME_DESC -> items.sortedWith(compareBy({ !it.isDirectory }, { -it.name.lowercase().hashCode() }))
            FileSortOption.DATE_DESC -> items.sortedWith(compareBy({ !it.isDirectory }, { -it.lastModified }))
            FileSortOption.DATE_ASC -> items.sortedWith(compareBy({ !it.isDirectory }, { it.lastModified }))
            FileSortOption.SIZE_DESC -> items.sortedWith(compareBy({ !it.isDirectory }, { -it.size }))
            FileSortOption.SIZE_ASC -> items.sortedWith(compareBy({ !it.isDirectory }, { it.size }))
        }
    }

    suspend fun createFile(dir: File, name: String, initialContent: String = ""): Result<File> = withContext(Dispatchers.IO) {
        try {
            val file = File(dir, name)
            if (file.exists()) return@withContext Result.failure(Exception("File already exists"))
            file.writeText(initialContent)
            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createDirectory(dir: File, name: String): Result<File> = withContext(Dispatchers.IO) {
        try {
            val subDir = File(dir, name)
            if (subDir.exists()) return@withContext Result.failure(Exception("Folder already exists"))
            if (subDir.mkdirs()) Result.success(subDir) else Result.failure(Exception("Failed to create folder"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun rename(file: File, newName: String): Result<File> = withContext(Dispatchers.IO) {
        try {
            val target = File(file.parentFile, newName)
            if (target.exists()) return@withContext Result.failure(Exception("A file with this name already exists"))
            if (file.renameTo(target)) Result.success(target) else Result.failure(Exception("Rename operation failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun delete(file: File): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val success = if (file.isDirectory) file.deleteRecursively() else file.delete()
            if (success) Result.success(true) else Result.failure(Exception("Delete failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun copy(source: File, targetDir: File): Result<File> = withContext(Dispatchers.IO) {
        try {
            val target = File(targetDir, source.name)
            if (source.isDirectory) {
                source.copyRecursively(target, overwrite = false)
            } else {
                source.copyTo(target, overwrite = false)
            }
            Result.success(target)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun move(source: File, targetDir: File): Result<File> = withContext(Dispatchers.IO) {
        try {
            val target = File(targetDir, source.name)
            if (source.renameTo(target)) {
                Result.success(target)
            } else {
                copy(source, targetDir).also { delete(source) }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun duplicate(file: File): Result<File> = withContext(Dispatchers.IO) {
        try {
            val nameWithoutExt = file.nameWithoutExtension
            val ext = if (file.extension.isNotEmpty()) ".${file.extension}" else ""
            var copyIndex = 1
            var candidate: File
            do {
                candidate = File(file.parentFile, "${nameWithoutExt}_copy$copyIndex$ext")
                copyIndex++
            } while (candidate.exists())

            if (file.isDirectory) {
                file.copyRecursively(candidate)
            } else {
                file.copyTo(candidate)
            }
            Result.success(candidate)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun search(root: File, query: String): List<File> = withContext(Dispatchers.IO) {
        val results = mutableListOf<File>()
        fun searchDir(dir: File) {
            dir.listFiles()?.forEach { file ->
                if (file.name.contains(query, ignoreCase = true)) {
                    results.add(file)
                }
                if (file.isDirectory && !file.name.startsWith(".")) {
                    searchDir(file)
                }
            }
        }
        searchDir(root)
        results
    }

    suspend fun calculateHash(file: File, algorithm: String = "SHA-256"): String = withContext(Dispatchers.IO) {
        try {
            if (file.isDirectory) return@withContext "Directory"
            val digest = MessageDigest.getInstance(algorithm)
            FileInputStream(file).use { fis ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (fis.read(buffer).also { bytesRead = it } != -1) {
                    digest.update(buffer, 0, bytesRead)
                }
            }
            digest.digest().joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            "Unavailable: ${e.message}"
        }
    }
}
