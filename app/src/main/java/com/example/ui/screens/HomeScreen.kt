package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.StorageStats
import com.example.core.WorkspaceManager
import com.example.data.ProjectEntity
import com.example.data.RecentFileEntity
import com.example.ui.components.GlassCard
import com.example.ui.components.StatusBadge
import java.io.File

@Composable
fun HomeScreen(
    workspaceManager: WorkspaceManager,
    projects: List<ProjectEntity>,
    recentFiles: List<RecentFileEntity>,
    terminalSessionCount: Int,
    storageStats: StorageStats,
    onNavigateToTab: (Int) -> Unit, // 0: Home, 1: Files, 2: Terminal, 3: Editor, 4: More
    onNavigateToMoreSubscreen: (String) -> Unit,
    onOpenProject: (ProjectEntity) -> Unit,
    onOpenFile: (File) -> Unit,
    onOpenCommandPalette: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Hero Branding Card
            GlassCard(
                borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.testTag("home_hero_card")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Terminal,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "NexVora Terminal",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Code • Run • Build • Anywhere",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Dev: Prince AR Abdur Rahman  •  NexVora Lab’s Ofc",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = onOpenCommandPalette,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .testTag("quick_search_btn")
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search Command Palette",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Environment Status Section
        item {
            Text(
                text = "Environment Status",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            GlassCard(modifier = Modifier.testTag("environment_status_card")) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatusBadge(label = "Terminal Subsystem Ready", isSuccess = true)
                        StatusBadge(label = "Storage Ready: ${storageStats.formatBytes(storageStats.freeBytes)} free", isSuccess = true)
                        StatusBadge(label = "Git Safe Engine Active", isSuccess = true)
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatusBadge(label = "Package Manager Ready", isSuccess = true)
                        StatusBadge(label = "Build Engine Ready", isSuccess = true)
                        StatusBadge(label = "$terminalSessionCount Active Sessions", isSuccess = terminalSessionCount > 0, isWarning = terminalSessionCount == 0)
                    }
                }
            }
        }

        // Quick Actions
        item {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    QuickActionChip(
                        icon = Icons.Default.AddBox,
                        label = "New Project",
                        onClick = { onNavigateToMoreSubscreen("projects") }
                    )
                }
                item {
                    QuickActionChip(
                        icon = Icons.Default.FolderOpen,
                        label = "Files",
                        onClick = { onNavigateToTab(1) }
                    )
                }
                item {
                    QuickActionChip(
                        icon = Icons.Default.Terminal,
                        label = "Terminal",
                        onClick = { onNavigateToTab(2) }
                    )
                }
                item {
                    QuickActionChip(
                        icon = Icons.Default.Code,
                        label = "Code Editor",
                        onClick = { onNavigateToTab(3) }
                    )
                }
                item {
                    QuickActionChip(
                        icon = Icons.Default.Memory,
                        label = "Packages",
                        onClick = { onNavigateToMoreSubscreen("packages") }
                    )
                }
                item {
                    QuickActionChip(
                        icon = Icons.Default.Commit,
                        label = "Git Tools",
                        onClick = { onNavigateToMoreSubscreen("git") }
                    )
                }
                item {
                    QuickActionChip(
                        icon = Icons.Default.PlayArrow,
                        label = "Build Project",
                        onClick = { onNavigateToMoreSubscreen("build") }
                    )
                }
            }
        }

        // Recent Projects
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Projects",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                TextButton(onClick = { onNavigateToMoreSubscreen("projects") }) {
                    Text("View All", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                }
            }
            if (projects.isEmpty()) {
                GlassCard {
                    Text(
                        text = "No projects created yet. Tap 'New Project' to create one.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    projects.take(3).forEach { project ->
                        GlassCard(
                            onClick = { onOpenProject(project) },
                            modifier = Modifier.testTag("project_item_${project.id}")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Folder,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = project.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${project.type.uppercase()} • ${project.path}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Icon(
                                    Icons.Default.ChevronRight,
                                    contentDescription = "Open",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Recent Files
        item {
            Text(
                text = "Recent Files",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (recentFiles.isEmpty()) {
                GlassCard {
                    Text(
                        text = "No recent files edited. Open a file from the Files tab to start coding.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    recentFiles.take(4).forEach { fileEntity ->
                        GlassCard(
                            onClick = { onOpenFile(File(fileEntity.path)) },
                            modifier = Modifier.testTag("recent_file_${fileEntity.name}")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.InsertDriveFile,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = fileEntity.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = fileEntity.path,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Workspace Storage Summary
        item {
            GlassCard(
                borderColor = MaterialTheme.colorScheme.outlineVariant,
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Storage Workspace",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${storageStats.formatBytes(storageStats.workspaceBytes)} used by NexVora",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "${storageStats.formatBytes(storageStats.freeBytes)} Available",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun QuickActionChip(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
