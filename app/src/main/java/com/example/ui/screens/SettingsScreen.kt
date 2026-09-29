package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.WorkspaceManager
import com.example.data.AppSettings
import com.example.data.SettingsDataStore
import com.example.ui.components.GlassCard
import com.example.ui.theme.AccentChoice
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    settings: AppSettings,
    settingsDataStore: SettingsDataStore,
    workspaceManager: WorkspaceManager,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Preferences & Settings",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Customize the developer workstation appearance and runtime behaviors",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Appearance
        item {
            GlassCard {
                Text("Appearance & Theme", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                Text("Theme Mode:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemeMode.values().forEach { mode ->
                        FilterChip(
                            selected = settings.themeMode == mode,
                            onClick = {
                                coroutineScope.launch { settingsDataStore.updateThemeMode(mode) }
                            },
                            label = { Text(mode.name) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("Primary Accent Color:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AccentChoice.values().forEach { accent ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(accent.primary)
                                .border(
                                    if (settings.accentChoice == accent) 3.dp else 1.dp,
                                    if (settings.accentChoice == accent) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.outline,
                                    CircleShape
                                )
                                .clickable {
                                    coroutineScope.launch { settingsDataStore.updateAccentChoice(accent) }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (settings.accentChoice == accent) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = androidx.compose.ui.graphics.Color.Black, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }

        // Terminal Settings
        item {
            GlassCard {
                Text("Terminal Subsystem", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Font Size: ${settings.terminalFontSize}sp", fontSize = 13.sp)
                    Row {
                        IconButton(onClick = {
                            if (settings.terminalFontSize > 10) {
                                coroutineScope.launch { settingsDataStore.updateTerminalFontSize(settings.terminalFontSize - 1) }
                            }
                        }) { Icon(Icons.Default.Remove, contentDescription = "Decrease") }
                        IconButton(onClick = {
                            if (settings.terminalFontSize < 24) {
                                coroutineScope.launch { settingsDataStore.updateTerminalFontSize(settings.terminalFontSize + 1) }
                            }
                        }) { Icon(Icons.Default.Add, contentDescription = "Increase") }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Haptic Feedback / Bell Vibration", fontSize = 13.sp)
                    Switch(
                        checked = settings.bellVibration,
                        onCheckedChange = {
                            coroutineScope.launch { settingsDataStore.updateBellVibration(it) }
                        }
                    )
                }
            }
        }

        // Code Editor Settings
        item {
            GlassCard {
                Text("Code Editor", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Editor Font Size: ${settings.editorFontSize}sp", fontSize = 13.sp)
                    Row {
                        IconButton(onClick = {
                            if (settings.editorFontSize > 10) {
                                coroutineScope.launch { settingsDataStore.updateEditorFontSize(settings.editorFontSize - 1) }
                            }
                        }) { Icon(Icons.Default.Remove, contentDescription = "Decrease") }
                        IconButton(onClick = {
                            if (settings.editorFontSize < 26) {
                                coroutineScope.launch { settingsDataStore.updateEditorFontSize(settings.editorFontSize + 1) }
                            }
                        }) { Icon(Icons.Default.Add, contentDescription = "Increase") }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Word Wrap", fontSize = 13.sp)
                    Switch(
                        checked = settings.wordWrap,
                        onCheckedChange = {
                            coroutineScope.launch { settingsDataStore.updateWordWrap(it) }
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Auto Save on Focus Loss", fontSize = 13.sp)
                    Switch(
                        checked = settings.autoSave,
                        onCheckedChange = {
                            coroutineScope.launch { settingsDataStore.updateAutoSave(it) }
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Tab Indentation: ${settings.tabSize} spaces", fontSize = 13.sp)
                    Row {
                        FilterChip(
                            selected = settings.tabSize == 2,
                            onClick = { coroutineScope.launch { settingsDataStore.updateTabSize(2) } },
                            label = { Text("2") }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        FilterChip(
                            selected = settings.tabSize == 4,
                            onClick = { coroutineScope.launch { settingsDataStore.updateTabSize(4) } },
                            label = { Text("4") }
                        )
                    }
                }
            }
        }

        // Maintenance & Reset
        item {
            GlassCard {
                Text("Maintenance & Storage", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))

                Text("Workspace Root: ${workspaceManager.rootDir.absolutePath}", fontSize = 11.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            Toast.makeText(context, "Temporary cache cleared.", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Clear Cache")
                    }
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                settingsDataStore.resetSettings()
                                Toast.makeText(context, "Preferences reset to defaults.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Reset All")
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
