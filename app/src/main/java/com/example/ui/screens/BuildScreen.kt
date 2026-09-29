package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.buildsystem.BuildEngine
import com.example.buildsystem.BuildStatus
import com.example.data.ProjectEntity
import com.example.ui.components.GlassCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.TextPrimaryDark
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuildScreen(
    projects: List<ProjectEntity>,
    buildEngine: BuildEngine,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedProject by remember { mutableStateOf(projects.firstOrNull()) }
    val buildStatus by buildEngine.status.collectAsState()
    val buildLogs by buildEngine.logs.collectAsState()
    var artifactsList by remember { mutableStateOf<List<File>>(emptyList()) }

    var expandedDropdown by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Build & Execution Engine",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Compile, assemble, or run your project files",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Project Selector
        ExposedDropdownMenuBox(
            expanded = expandedDropdown,
            onExpandedChange = { expandedDropdown = it }
        ) {
            OutlinedTextField(
                value = selectedProject?.name ?: "No project selected",
                onValueChange = {},
                readOnly = true,
                label = { Text("Target Project") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expandedDropdown,
                onDismissRequest = { expandedDropdown = false }
            ) {
                projects.forEach { proj ->
                    DropdownMenuItem(
                        text = { Text(proj.name) },
                        onClick = {
                            selectedProject = proj
                            expandedDropdown = false
                        }
                    )
                }
            }
        }

        if (selectedProject == null) {
            GlassCard(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Select a project to initiate build or execution.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            return
        }

        val projDir = File(selectedProject!!.path)

        // Build Trigger Card
        GlassCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Project: ${selectedProject!!.name}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Type: ${selectedProject!!.type.uppercase()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                when (buildStatus) {
                    BuildStatus.BUILDING -> StatusBadge(label = "Building...", isWarning = true)
                    BuildStatus.SUCCESS -> StatusBadge(label = "Build Succeeded", isSuccess = true)
                    BuildStatus.FAILED -> StatusBadge(label = "Build Failed", isSuccess = false)
                    BuildStatus.IDLE -> StatusBadge(label = "Ready to Build", isSuccess = true)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val res = buildEngine.build(projDir)
                            artifactsList = res.artifacts
                        }
                    },
                    enabled = buildStatus != BuildStatus.BUILDING,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.weight(1f).testTag("start_build_btn")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (buildStatus == BuildStatus.BUILDING) "Building..." else "Build / Run", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { buildEngine.clear() },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Clear")
                }
            }
        }

        // Output Logs Console
        Text(
            text = "Build Output & Logs",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Surface(
            color = DarkBackground,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
        ) {
            if (buildLogs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Build output will be displayed here in real-time.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(buildLogs) { logLine ->
                        Text(
                            text = logLine,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = when {
                                logLine.startsWith("✓") -> MaterialTheme.colorScheme.primary
                                logLine.startsWith("✗") -> MaterialTheme.colorScheme.error
                                logLine.startsWith("!") -> MaterialTheme.colorScheme.secondary
                                else -> TextPrimaryDark
                            }
                        )
                    }
                }
            }
        }

        // Build Artifacts (if any)
        if (artifactsList.isNotEmpty()) {
            Text(
                text = "Generated Artifacts (${artifactsList.size})",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                artifactsList.forEach { art ->
                    GlassCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Inventory2, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(art.name, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                            Spacer(modifier = Modifier.weight(1f))
                            Text("${art.length()} B", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
