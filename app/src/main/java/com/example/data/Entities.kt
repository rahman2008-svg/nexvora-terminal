package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val path: String,
    val type: String, // python, nodejs, c, cpp, java, kotlin, web, bash, custom
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val gitInitialized: Boolean = false
)

@Entity(tableName = "recent_files")
data class RecentFileEntity(
    @PrimaryKey
    val path: String,
    val name: String,
    val language: String,
    val lastOpenedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "terminal_sessions")
data class TerminalSessionEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val workingDir: String,
    val createdAt: Long = System.currentTimeMillis(),
    val lastActiveTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "packages")
data class PackageEntity(
    @PrimaryKey
    val id: String, // e.g. "sys:git", "pip:requests", "npm:express"
    val name: String,
    val version: String,
    val type: String, // sys, pip, npm
    val description: String,
    val installedDate: Long = System.currentTimeMillis(),
    val installed: Boolean = true
)
