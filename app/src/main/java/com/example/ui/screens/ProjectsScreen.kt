package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ProjectEntity
import com.example.projects.ProjectManager
import com.example.projects.ProjectType
import com.example.ui.components.EmptyStateView
import com.example.ui.components.GlassCard
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProjectsScreen(
    projects: List<ProjectEntity>,
    projectManager: ProjectManager,
    onOpenProjectFiles: (ProjectEntity) -> Unit,
    onOpenProjectTerminal: (ProjectEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var showCreateDialog by remember { mutableStateOf(false) }
    var projectToDelete by remember { mutableStateOf<ProjectEntity?>(null) }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Project Manager",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${projects.size} local projects in NexVora workspace",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Button(
                onClick = { showCreateDialog = true },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.testTag("new_project_main_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("New", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (projects.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.FolderOpen,
                title = "No Projects Yet",
                description = "Create a project from templates (Python, Web, C, Node.js, Bash) to start developing.",
                actionLabel = "Create First Project",
                onAction = { showCreateDialog = true },
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(projects) { project ->
                    GlassCard(
                        modifier = Modifier.fillMaxWidth().testTag("project_card_${project.name}")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when (project.type.lowercase()) {
                                        "python" -> "🐍"
                                        "web" -> "🌐"
                                        "nodejs" -> "🟢"
                                        "c", "cpp" -> "⚙️"
                                        "java" -> "☕"
                                        "bash" -> "🐚"
                                        else -> "📁"
                                    },
                                    fontSize = 20.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = project.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${project.type.uppercase()} • ${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(project.createdAt))}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Open in files
                            IconButton(onClick = { onOpenProjectFiles(project) }) {
                                Icon(Icons.Default.FolderOpen, contentDescription = "Files", tint = MaterialTheme.colorScheme.primary)
                            }
                            // Open terminal
                            IconButton(onClick = { onOpenProjectTerminal(project) }) {
                                Icon(Icons.Default.Terminal, contentDescription = "Terminal", tint = MaterialTheme.colorScheme.secondary)
                            }
                            // Delete
                            IconButton(onClick = { projectToDelete = project }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }

    // Create Project Dialog
    if (showCreateDialog) {
        var projectName by remember { mutableStateOf("") }
        var selectedType by remember { mutableStateOf(ProjectType.PYTHON) }
        var initGit by remember { mutableStateOf(true) }
        var generateReadme by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Create New Project") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = projectName,
                        onValueChange = { projectName = it },
                        label = { Text("Project Name") },
                        placeholder = { Text("e.g. my-app, scanner, script") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("create_project_name_input")
                    )

                    Text("Project Template:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Column {
                        listOf(
                            ProjectType.PYTHON,
                            ProjectType.WEB,
                            ProjectType.NODEJS,
                            ProjectType.C,
                            ProjectType.BASH,
                            ProjectType.EMPTY
                        ).forEach { type ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedType = type }
                                    .padding(vertical = 4.dp)
                            ) {
                                RadioButton(
                                    selected = selectedType == type,
                                    onClick = { selectedType = type }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("${type.icon} ${type.label}", fontSize = 13.sp)
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = initGit, onCheckedChange = { initGit = it })
                        Text("Initialize Git repository", fontSize = 12.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = generateReadme, onCheckedChange = { generateReadme = it })
                        Text("Generate README.md & .gitignore", fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (projectName.isNotBlank()) {
                            coroutineScope.launch {
                                val result = projectManager.createProject(
                                    name = projectName.trim(),
                                    type = selectedType,
                                    initGit = initGit,
                                    generateReadme = generateReadme
                                )
                                if (result.isSuccess) {
                                    Toast.makeText(context, "✓ Project '${projectName.trim()}' created!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Error: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                        showCreateDialog = false
                    },
                    modifier = Modifier.testTag("confirm_create_project_btn")
                ) { Text("Create Project") }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Delete confirmation
    if (projectToDelete != null) {
        val proj = projectToDelete!!
        AlertDialog(
            onDismissRequest = { projectToDelete = null },
            title = { Text("Delete Project") },
            text = { Text("Are you sure you want to permanently delete project '${proj.name}' and all its source files?") },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            projectManager.deleteProject(proj)
                            Toast.makeText(context, "Project deleted", Toast.LENGTH_SHORT).show()
                        }
                        projectToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { projectToDelete = null }) { Text("Cancel") }
            }
        )
    }
}
