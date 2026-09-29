package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard

data class MoreItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector
)

@Composable
fun MoreScreen(
    onNavigateToSubscreen: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        MoreItem("projects", "Projects", "Manage & create project templates", Icons.Default.FolderSpecial),
        MoreItem("packages", "Packages", "System, pip & npm package manager", Icons.Default.Memory),
        MoreItem("git", "Git & GitHub", "Branches, commits, clone & status", Icons.Default.Commit),
        MoreItem("build", "Build & Run", "Auto compiler & script runner", Icons.Default.PlayArrow),
        MoreItem("runtimes", "Runtimes", "Python, Node, C, Java detectors", Icons.Default.Terminal),
        MoreItem("server", "Local Server", "Embedded 127.0.0.1 HTTP host", Icons.Default.Sensors),
        MoreItem("devtools", "Developer Tools", "JSON, Base64, Regex, HTTP tester", Icons.Default.Build),
        MoreItem("settings", "Settings", "Appearance, fonts & editor configs", Icons.Default.Settings),
        MoreItem("about", "About NexVora", "Author, publisher & build info", Icons.Default.Info)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "Developer Workstation Suite",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Select a tool or system module to open",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(items) { item ->
                GlassCard(
                    onClick = { onNavigateToSubscreen(item.id) },
                    modifier = Modifier.fillMaxWidth().testTag("more_item_${item.id}")
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = item.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.subtitle,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}
