package com.example.editor

import java.io.File
import java.util.UUID

data class EditorTab(
    val id: String = UUID.randomUUID().toString(),
    val file: File,
    var content: String,
    var isDirty: Boolean = false,
    val undoStack: MutableList<String> = mutableListOf(),
    val redoStack: MutableList<String> = mutableListOf(),
    var language: String = detectLanguage(file.name),
    var encoding: String = "UTF-8"
) {
    companion object {
        fun detectLanguage(filename: String): String {
            return when (filename.substringAfterLast(".", "").lowercase()) {
                "py" -> "python"
                "js" -> "javascript"
                "ts" -> "typescript"
                "html", "htm" -> "html"
                "css" -> "css"
                "json" -> "json"
                "md" -> "markdown"
                "sh", "bash" -> "bash"
                "c", "h" -> "c"
                "cpp", "hpp", "cc" -> "cpp"
                "java" -> "java"
                "kt", "kts" -> "kotlin"
                "xml" -> "xml"
                "sql" -> "sql"
                "yaml", "yml" -> "yaml"
                "toml" -> "toml"
                else -> "text"
            }
        }
    }

    fun modify(newContent: String) {
        if (newContent != content) {
            undoStack.add(content)
            if (undoStack.size > 50) undoStack.removeAt(0)
            redoStack.clear()
            content = newContent
            isDirty = true
        }
    }

    fun undo(): Boolean {
        if (undoStack.isNotEmpty()) {
            val previous = undoStack.removeAt(undoStack.size - 1)
            redoStack.add(content)
            content = previous
            isDirty = true
            return true
        }
        return false
    }

    fun redo(): Boolean {
        if (redoStack.isNotEmpty()) {
            val next = redoStack.removeAt(redoStack.size - 1)
            undoStack.add(content)
            content = next
            isDirty = true
            return true
        }
        return false
    }

    fun save(): Boolean {
        return try {
            file.writeText(content, charset(encoding))
            isDirty = false
            true
        } catch (_: Exception) {
            false
        }
    }
}
