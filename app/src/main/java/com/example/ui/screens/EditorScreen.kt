package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.editor.EditorTab
import com.example.editor.SyntaxHighlighter
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark

@Composable
fun EditorScreen(
    tabs: List<EditorTab>,
    activeTabIndex: Int,
    onSelectTab: (Int) -> Unit,
    onCloseTab: (Int) -> Unit,
    onOpenFilesRequested: () -> Unit,
    editorFontSize: Int = 14,
    wordWrap: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activeTab = tabs.getOrNull(activeTabIndex)

    // Search & Replace state
    var showSearchReplace by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var replaceQuery by remember { mutableStateOf("") }

    if (activeTab == null) {
        EmptyStateView(
            icon = Icons.Default.Code,
            title = "No File Open",
            description = "Select a file from the Files tab or open a project to start editing code.",
            actionLabel = "Browse Files",
            onAction = onOpenFilesRequested,
            modifier = modifier.fillMaxSize()
        )
        return
    }

    var textContent by remember(activeTab.id) { mutableStateOf(activeTab.content) }

    fun updateContent(newText: String) {
        textContent = newText
        activeTab.modify(newText)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Tab Row
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 2.dp
        ) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(tabs.indices.toList()) { index ->
                    val tab = tabs[index]
                    val isSelected = index == activeTabIndex

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
                            .clickable { onSelectTab(index) }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = (if (tab.isDirty) "● " else "") + tab.file.name,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .size(14.dp)
                                .clickable { onCloseTab(index) }
                        )
                    }
                }
            }
        }

        // Action Toolbar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Save Button
                IconButton(
                    onClick = {
                        val saved = activeTab.save()
                        Toast.makeText(
                            context,
                            if (saved) "✓ File saved: ${activeTab.file.name}" else "Failed to save file",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    modifier = Modifier.size(36.dp).testTag("editor_save_btn")
                ) {
                    Icon(
                        Icons.Default.Save,
                        contentDescription = "Save",
                        tint = if (activeTab.isDirty) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Undo Button
                IconButton(
                    onClick = {
                        if (activeTab.undo()) {
                            textContent = activeTab.content
                        }
                    },
                    enabled = activeTab.undoStack.isNotEmpty(),
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Undo,
                        contentDescription = "Undo",
                        tint = if (activeTab.undoStack.isNotEmpty()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Redo Button
                IconButton(
                    onClick = {
                        if (activeTab.redo()) {
                            textContent = activeTab.content
                        }
                    },
                    enabled = activeTab.redoStack.isNotEmpty(),
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Redo,
                        contentDescription = "Redo",
                        tint = if (activeTab.redoStack.isNotEmpty()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Search & Replace Toggle
                IconButton(
                    onClick = { showSearchReplace = !showSearchReplace },
                    modifier = Modifier.size(36.dp).testTag("editor_search_btn")
                ) {
                    Icon(
                        Icons.Default.FindReplace,
                        contentDescription = "Find and Replace",
                        tint = if (showSearchReplace) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Language and Encoding badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${activeTab.language.uppercase()} • ${activeTab.encoding}",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Search & Replace Bar
        if (showSearchReplace) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Find...", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f).height(46.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        OutlinedTextField(
                            value = replaceQuery,
                            onValueChange = { replaceQuery = it },
                            placeholder = { Text("Replace...", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f).height(46.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Button(
                            onClick = {
                                if (searchQuery.isNotEmpty()) {
                                    val replaced = textContent.replace(searchQuery, replaceQuery)
                                    updateContent(replaced)
                                    Toast.makeText(context, "Replaced occurrences", Toast.LENGTH_SHORT).show()
                                }
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("All", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Code Editor View with Synchronized Line Numbers
        val lines = remember(textContent) { textContent.split("\n") }
        val lineCount = lines.size
        val verticalScrollState = rememberScrollState()
        val horizontalScrollState = rememberScrollState()

        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(verticalScrollState)
        ) {
            // Line numbers column
            Column(
                modifier = Modifier
                    .width(42.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.End
            ) {
                for (i in 1..lineCount) {
                    Text(
                        text = "$i ",
                        fontSize = editorFontSize.sp,
                        lineHeight = (editorFontSize + 6).sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextMutedDark
                    )
                }
            }

            // Code Text Field
            val codeModifier = if (wordWrap) {
                Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            } else {
                Modifier
                    .weight(1f)
                    .horizontalScroll(horizontalScrollState)
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            }

            BasicTextField(
                value = textContent,
                onValueChange = { updateContent(it) },
                textStyle = TextStyle(
                    color = TextPrimaryDark,
                    fontFamily = FontFamily.Monospace,
                    fontSize = editorFontSize.sp,
                    lineHeight = (editorFontSize + 6).sp
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = codeModifier.testTag("editor_text_area")
            )
        }
    }
}
