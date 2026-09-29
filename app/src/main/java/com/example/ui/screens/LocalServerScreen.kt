package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.LocalHttpServer
import com.example.core.WorkspaceManager
import com.example.data.ProjectEntity
import com.example.ui.components.GlassCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.TextPrimaryDark
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalServerScreen(
    server: LocalHttpServer,
    workspaceManager: WorkspaceManager,
    projects: List<ProjectEntity>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val status by server.status.collectAsState()
    val logs by server.logs.collectAsState()

    var selectedDir by remember {
        val webProj = projects.firstOrNull { it.type == "web" }
        mutableStateOf(if (webProj != null) File(webProj.path) else workspaceManager.projectsDir)
    }
    var portText by remember { mutableStateOf(server.port.toString()) }
    var expandedDropdown by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column {
            Text(
                text = "Local Web Server",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Serve static sites & APIs locally on 127.0.0.1",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Server Control Card
        GlassCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Status: ${status.name}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Serving: ${selectedDir.name}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                when (status) {
                    LocalHttpServer.ServerStatus.RUNNING -> StatusBadge("Online", isSuccess = true)
                    LocalHttpServer.ServerStatus.STARTING -> StatusBadge("Starting", isWarning = true)
                    LocalHttpServer.ServerStatus.ERROR -> StatusBadge("Error", isSuccess = false)
                    LocalHttpServer.ServerStatus.STOPPED -> StatusBadge("Stopped", isWarning = true)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Directory Selector
            ExposedDropdownMenuBox(
                expanded = expandedDropdown,
                onExpandedChange = { expandedDropdown = it }
            ) {
                OutlinedTextField(
                    value = selectedDir.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Web Project Directory") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedDropdown,
                    onDismissRequest = { expandedDropdown = false }
                ) {
                    projects.forEach { p ->
                        DropdownMenuItem(
                            text = { Text("${p.name} (${p.type})") },
                            onClick = {
                                selectedDir = File(p.path)
                                expandedDropdown = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Port Config & Open in Browser
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = portText,
                    onValueChange = { portText = it },
                    label = { Text("Port") },
                    singleLine = true,
                    enabled = status != LocalHttpServer.ServerStatus.RUNNING,
                    modifier = Modifier.width(100.dp)
                )

                if (status == LocalHttpServer.ServerStatus.RUNNING) {
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://127.0.0.1:${server.port}/"))
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Preview Site", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                if (status == LocalHttpServer.ServerStatus.RUNNING) {
                    Button(
                        onClick = { server.stop() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Stop Server")
                    }
                    OutlinedButton(
                        onClick = {
                            val p = portText.toIntOrNull() ?: 8080
                            server.restart(selectedDir, p)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Restart")
                    }
                } else {
                    Button(
                        onClick = {
                            val p = portText.toIntOrNull() ?: 8080
                            server.start(selectedDir, p)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("start_server_btn")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Start Server (http://127.0.0.1:${portText.toIntOrNull() ?: 8080})", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Live HTTP Logs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Access Logs (${logs.size})",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            TextButton(onClick = { server.clearLogs() }) {
                Text("Clear Logs", fontSize = 12.sp)
            }
        }

        Surface(
            color = DarkBackground,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
        ) {
            if (logs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("HTTP request and access logs will stream here.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(logs) { log ->
                        Text(
                            text = log,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = if (log.contains("200")) MaterialTheme.colorScheme.primary else if (log.contains("404")) MaterialTheme.colorScheme.error else TextPrimaryDark
                        )
                    }
                }
            }
        }
    }
}
