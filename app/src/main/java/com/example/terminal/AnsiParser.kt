package com.example.terminal

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import com.example.ui.theme.*

object AnsiParser {

    private val ansiRegex = Regex("\u001B\\[[0-9;]*[a-zA-Z]")

    fun parseAnsi(text: String, defaultColor: Color = TextPrimaryDark): AnnotatedString {
        return buildAnnotatedString {
            var currentIndex = 0
            var currentColor = defaultColor
            var isBold = false

            val matches = ansiRegex.findAll(text).toList()
            if (matches.isEmpty()) {
                append(text)
                return@buildAnnotatedString
            }

            for (match in matches) {
                // Append text before this escape code
                if (match.range.first > currentIndex) {
                    val segment = text.substring(currentIndex, match.range.first)
                    val start = length
                    append(segment)
                    addStyle(
                        SpanStyle(
                            color = currentColor,
                            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
                        ),
                        start,
                        length
                    )
                }

                // Process ANSI code
                val code = match.value
                if (code.endsWith("m")) {
                    val params = code.removePrefix("\u001B[").removeSuffix("m")
                    if (params.isEmpty() || params == "0") {
                        currentColor = defaultColor
                        isBold = false
                    } else {
                        val tokens = params.split(";")
                        for (token in tokens) {
                            when (token.toIntOrNull()) {
                                0 -> { currentColor = defaultColor; isBold = false }
                                1 -> isBold = true
                                30 -> currentColor = AnsiBrightBlack
                                31 -> currentColor = AnsiRed
                                32 -> currentColor = AnsiGreen
                                33 -> currentColor = AnsiYellow
                                34 -> currentColor = AnsiBlue
                                35 -> currentColor = AnsiMagenta
                                36 -> currentColor = AnsiCyan
                                37 -> currentColor = AnsiWhite
                                90 -> currentColor = AnsiBrightBlack
                                91 -> currentColor = AnsiRed
                                92 -> currentColor = AnsiBrightGreen
                                93 -> currentColor = AnsiYellow
                                94 -> currentColor = AnsiBlue
                                95 -> currentColor = AnsiMagenta
                                96 -> currentColor = AnsiBrightCyan
                                97 -> currentColor = Color.White
                                39 -> currentColor = defaultColor
                            }
                        }
                    }
                }

                currentIndex = match.range.last + 1
            }

            // Append any remaining text
            if (currentIndex < text.length) {
                val segment = text.substring(currentIndex)
                val start = length
                append(segment)
                addStyle(
                    SpanStyle(
                        color = currentColor,
                        fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
                    ),
                    start,
                    length
                )
            }
        }
    }
}
