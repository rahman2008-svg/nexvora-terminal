package com.example.editor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import com.example.ui.theme.*

object SyntaxHighlighter {

    private val KEYWORD_COLOR = Color(0xFFC678DD) // Purple
    private val STRING_COLOR = Color(0xFF98C379)  // Green
    private val COMMENT_COLOR = Color(0xFF5C6370) // Gray / Muted
    private val NUMBER_COLOR = Color(0xFFD19A66)  // Orange
    private val TYPE_COLOR = Color(0xFFE5C07B)    // Yellow
    private val FUNCTION_COLOR = Color(0xFF61AFEF)// Blue
    private val OPERATOR_COLOR = Color(0xFF56B6C2)// Cyan

    private val PYTHON_KEYWORDS = setOf(
        "def", "class", "if", "elif", "else", "for", "while", "return", "import", "from",
        "as", "try", "except", "finally", "with", "lambda", "yield", "pass", "break",
        "continue", "raise", "assert", "global", "nonlocal", "async", "await", "True", "False", "None"
    )

    private val JS_KEYWORDS = setOf(
        "function", "const", "let", "var", "if", "else", "for", "while", "do", "switch",
        "case", "default", "return", "try", "catch", "finally", "throw", "class", "extends",
        "new", "import", "export", "from", "async", "await", "typeof", "instanceof", "this",
        "true", "false", "null", "undefined", "interface", "type", "enum"
    )

    private val C_CPP_JAVA_KEYWORDS = setOf(
        "int", "float", "double", "char", "void", "bool", "boolean", "long", "short",
        "class", "public", "private", "protected", "static", "final", "const", "struct",
        "if", "else", "for", "while", "do", "switch", "case", "break", "continue", "return",
        "import", "package", "include", "new", "this", "super", "try", "catch", "throw", "true", "false"
    )

    private val KOTLIN_KEYWORDS = setOf(
        "fun", "val", "var", "class", "object", "interface", "data", "sealed", "override",
        "if", "else", "when", "for", "while", "return", "import", "package", "try", "catch",
        "throw", "is", "in", "as", "null", "true", "false", "suspend", "by", "companion"
    )

    private val SQL_KEYWORDS = setOf(
        "SELECT", "FROM", "WHERE", "INSERT", "INTO", "VALUES", "UPDATE", "SET", "DELETE",
        "CREATE", "TABLE", "DROP", "ALTER", "PRIMARY", "KEY", "FOREIGN", "NOT", "NULL",
        "ORDER", "BY", "GROUP", "HAVING", "LIMIT", "JOIN", "INNER", "LEFT", "RIGHT", "ON",
        "select", "from", "where", "insert", "into", "values", "update", "set", "delete"
    )

    fun highlight(code: String, language: String): AnnotatedString {
        val keywords = when (language.lowercase()) {
            "python", "py" -> PYTHON_KEYWORDS
            "javascript", "js", "typescript", "ts" -> JS_KEYWORDS
            "c", "cpp", "h", "hpp", "java" -> C_CPP_JAVA_KEYWORDS
            "kotlin", "kt" -> KOTLIN_KEYWORDS
            "sql" -> SQL_KEYWORDS
            else -> JS_KEYWORDS
        }

        return buildAnnotatedString {
            append(code)

            // Numbers
            val numberRegex = Regex("\\b\\d+(\\.\\d+)?\\b")
            for (match in numberRegex.findAll(code)) {
                addStyle(SpanStyle(color = NUMBER_COLOR), match.range.first, match.range.last + 1)
            }

            // Keywords
            val wordRegex = Regex("\\b[a-zA-Z_][a-zA-Z0-9_]*\\b")
            for (match in wordRegex.findAll(code)) {
                val word = match.value
                if (keywords.contains(word)) {
                    addStyle(
                        SpanStyle(color = KEYWORD_COLOR, fontWeight = FontWeight.SemiBold),
                        match.range.first,
                        match.range.last + 1
                    )
                }
            }

            // String literals
            val stringRegex = Regex("\"(\\\\.|[^\"])*\"|'(\\\\.|[^'])*'")
            for (match in stringRegex.findAll(code)) {
                addStyle(SpanStyle(color = STRING_COLOR), match.range.first, match.range.last + 1)
            }

            // Comments
            val lineComment = when (language.lowercase()) {
                "python", "py", "bash", "sh", "yaml", "yml", "toml" -> Regex("#.*")
                "sql" -> Regex("--.*")
                else -> Regex("//.*")
            }
            for (match in lineComment.findAll(code)) {
                addStyle(SpanStyle(color = COMMENT_COLOR), match.range.first, match.range.last + 1)
            }

            // Block comments (C, JS, Java, Kotlin)
            val blockComment = Regex("/\\*[\\s\\S]*?\\*/")
            for (match in blockComment.findAll(code)) {
                addStyle(SpanStyle(color = COMMENT_COLOR), match.range.first, match.range.last + 1)
            }
        }
    }
}
