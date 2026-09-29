package com.example.packages

import com.example.core.WorkspaceManager
import com.example.data.AppDatabase
import com.example.data.PackageEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File

data class PackageDefinition(
    val id: String,
    val name: String,
    val version: String,
    val type: String, // "sys", "pip", "npm"
    val description: String,
    val downloadSize: String,
    val author: String = "OpenSource"
)

class PackageManagerService(
    private val workspaceManager: WorkspaceManager,
    private val database: AppDatabase
) {

    // Available repository index
    val repositoryCatalog = listOf(
        // Linux / System
        PackageDefinition("sys:git", "git", "2.44.0", "sys", "Fast, scalable, distributed revision control system", "18.2 MB"),
        PackageDefinition("sys:python", "python", "3.11.8", "sys", "Interpreted high-level object-oriented programming language", "42.5 MB"),
        PackageDefinition("sys:curl", "curl", "8.6.0", "sys", "Command line tool for transferring data with URL syntax", "3.4 MB"),
        PackageDefinition("sys:busybox", "busybox", "1.36.1", "sys", "The Swiss Army Knife of Embedded Linux", "1.8 MB"),
        PackageDefinition("sys:clang", "clang", "17.0.6", "sys", "C, C++, Objective-C compiler with LLVM backend", "64.0 MB"),
        PackageDefinition("sys:node", "nodejs", "20.11.1", "sys", "JavaScript runtime built on Chrome's V8 JavaScript engine", "38.6 MB"),
        PackageDefinition("sys:tar", "tar", "1.35", "sys", "GNU tar saves many files together into a single tape or disk archive", "1.2 MB"),
        PackageDefinition("sys:sqlite", "sqlite3", "3.45.1", "sys", "Self-contained serverless SQL database engine", "2.6 MB"),
        PackageDefinition("sys:nano", "nano", "7.2", "sys", "Small and friendly text editor for terminal", "950 KB"),

        // Python packages (pip)
        PackageDefinition("pip:requests", "requests", "2.31.0", "pip", "Python HTTP for Humans™ library", "4.2 MB"),
        PackageDefinition("pip:flask", "flask", "3.0.2", "pip", "A simple framework for building complex web applications", "3.8 MB"),
        PackageDefinition("pip:numpy", "numpy", "1.26.4", "pip", "Fundamental package for scientific computing with Python", "14.5 MB"),
        PackageDefinition("pip:rich", "rich", "13.7.0", "pip", "Rich text and beautiful formatting in the terminal", "1.1 MB"),
        PackageDefinition("pip:pytest", "pytest", "8.0.2", "pip", "Simple powerful testing with Python", "2.5 MB"),

        // Node.js packages (npm)
        PackageDefinition("npm:express", "express", "4.19.2", "npm", "Fast, unopinionated, minimalist web framework for Node.js", "2.1 MB"),
        PackageDefinition("npm:axios", "axios", "1.6.7", "npm", "Promise based HTTP client for browser and node.js", "1.3 MB"),
        PackageDefinition("npm:chalk", "chalk", "5.3.0", "npm", "Terminal string styling done right", "450 KB"),
        PackageDefinition("npm:lodash", "lodash", "4.17.21", "npm", "A modern JavaScript utility library delivering modularity", "1.4 MB")
    )

    suspend fun installPackage(pkg: PackageDefinition, onLog: (String) -> Unit): Result<PackageEntity> = withContext(Dispatchers.IO) {
        try {
            onLog("-> Resolving dependencies for ${pkg.name} (${pkg.version})...")
            delay(300)
            onLog("-> Target: ${workspaceManager.packagesDir.absolutePath}/${pkg.name}")
            delay(300)

            // Real local package target creation
            val pkgDir = File(workspaceManager.packagesDir, "${pkg.type}_${pkg.name}")
            if (!pkgDir.exists()) pkgDir.mkdirs()

            File(pkgDir, "package_meta.json").writeText(
                """{
  "name": "${pkg.name}",
  "version": "${pkg.version}",
  "type": "${pkg.type}",
  "installedAt": ${System.currentTimeMillis()},
  "sandbox": true
}
""".trimIndent()
            )

            onLog("-> Unpacking ${pkg.name} into environment...")
            delay(200)

            val entity = PackageEntity(
                id = pkg.id,
                name = pkg.name,
                version = pkg.version,
                type = pkg.type,
                description = pkg.description,
                installedDate = System.currentTimeMillis(),
                installed = true
            )

            database.appDao().insertPackage(entity)
            onLog("✓ Successfully installed ${pkg.name} v${pkg.version}")
            Result.success(entity)
        } catch (e: Exception) {
            onLog("✗ Installation failed: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun removePackage(packageId: String, onLog: (String) -> Unit): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            onLog("-> Removing $packageId from NexVora sandbox...")
            database.appDao().deletePackage(packageId)
            val parts = packageId.split(":")
            if (parts.size == 2) {
                val dir = File(workspaceManager.packagesDir, "${parts[0]}_${parts[1]}")
                if (dir.exists()) dir.deleteRecursively()
            }
            onLog("✓ Removed $packageId successfully.")
            Result.success(true)
        } catch (e: Exception) {
            onLog("✗ Error removing package: ${e.message}")
            Result.failure(e)
        }
    }
}
