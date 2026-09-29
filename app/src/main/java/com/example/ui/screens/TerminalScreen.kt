package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.terminal.AnsiParser
import com.example.terminal.TerminalSession
import com.example.ui.components.ExtraKeyRow
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.TextPrimaryDark
import kotlinx.coroutines.launch

@Composable
fun TerminalScreen(
    sessions: List<TerminalSession>,
    activeSessionIndex: Int,
    onSelectSession: (Int) -> Unit,
    onAddSession: () -> Unit,
    onCloseSession: (Int) -> Unit,
    terminalFontSize: Int = 13,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val activeSession = sessions.getOrNull(activeSessionIndex) ?: return

    val lines by activeSession.lines.collectAsState()
    val isRunning by activeSession.isRunning.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto-scroll to bottom when new line arrives
    LaunchedEffect(lines.size) {
        if (lines.isNotEmpty()) {
            listState.animateScrollToItem(lines.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Tab Bar
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LazyRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(sessions.indices.toList()) { index ->
                        val session = sessions[index]
                        val isSelected = index == activeSessionIndex

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
                                .clickable { onSelectSession(index) }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = session.title,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (sessions.size > 1) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Close tab",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clickable { onCloseSession(index) }
                                )
                            }
                        }
                    }
                }

                IconButton(
                    onClick = onAddSession,
                    modifier = Modifier.size(32.dp).testTag("add_terminal_tab_btn")
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "New Terminal Session",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = { activeSession.clear() },
                    modifier = Modifier.size(32.dp).testTag("clear_terminal_btn")
                ) {
                    Icon(
                        Icons.Default.LayersClear,
                        contentDescription = "Clear",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = { activeSession.interrupt() },
                    modifier = Modifier.size(32.dp).testTag("interrupt_terminal_btn")
                ) {
                    Icon(
                        Icons.Default.Cancel,
                        contentDescription = "Interrupt",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Terminal Output Console
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            items(lines, key = { it.id }) { line ->
                val annotated = remember(line.rawText) {
                    AnsiParser.parseAnsi(line.rawText, TextPrimaryDark)
                }
                Text(
                    text = annotated,
                    fontFamily = FontFamily.Monospace,
                    fontSize = terminalFontSize.sp,
                    lineHeight = (terminalFontSize + 5).sp
                )
            }
        }

        // Extra Keys Bar
        ExtraKeyRow(
            onKeyPressed = { key ->
                when (key) {
                    "CTRL" -> inputText += "^"
                    "ALT" -> inputText += "M-"
                    "TAB" -> {
                        // Tab completion trigger
                        val parts = inputText.split(" ")
                        val last = parts.lastOrNull() ?: ""
                        val candidates = activeSession.workingDir.listFiles()?.map { it.name } ?: emptyList()
                        val match = candidates.firstOrNull { it.startsWith(last) }
                        if (match != null) {
                            inputText = (parts.dropLast(1) + match).joinToString(" ")
                        }
                    }
                    "ESC" -> activeSession.interrupt()
                    "↑" -> activeSession.getPreviousCommand()?.let { inputText = it }
                    "↓" -> activeSession.getNextCommand()?.let { inputText = it }
                    "←", "→" -> {}
                    else -> inputText += key
                }
            }
        )

        // Terminal Prompt & Input Row
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$ ",
                    color = MaterialTheme.colorScheme.primary,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = terminalFontSize.sp
                )
                BasicTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = FontFamily.Monospace,
                        fontSize = terminalFontSize.sp
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        autoCorrect = false,
                        imeAction = ImeAction.Send
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (inputText.isNotBlank()) {
                                activeSession.execute(inputText)
                                inputText = ""
                            }
                        }
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("terminal_input_field")
                )

                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            activeSession.execute(inputText)
                            inputText = ""
                        }
                    },
                    modifier = Modifier.size(36.dp).testTag("terminal_send_btn")
                ) {
                    Icon(
                        Icons.Default.KeyboardReturn,
                        contentDescription = "Execute Command",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
