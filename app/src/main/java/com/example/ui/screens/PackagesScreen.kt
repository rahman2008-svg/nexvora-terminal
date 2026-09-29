package com.example.ui.screens

import android.widget.Toast
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
import com.example.data.PackageEntity
import com.example.packages.PackageDefinition
import com.example.packages.PackageManagerService
import com.example.ui.components.GlassCard
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.TextPrimaryDark
import kotlinx.coroutines.launch

@Composable
fun PackagesScreen(
    packageService: PackageManagerService,
    installedPackages: List<PackageEntity>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Available, 1: Installed, 2: Logs
    var selectedCategory by remember { mutableStateOf("all") } // all, sys, pip, npm
    var searchQuery by remember { mutableStateOf("") }
    var logs by remember { mutableStateOf<List<String>>(emptyList()) }
    var isInstalling by remember { mutableStateOf(false) }

    fun addLog(msg: String) {
        logs = logs + msg
    }

    val availableList = remember(searchQuery, selectedCategory) {
        packageService.repositoryCatalog.filter { pkg ->
            (selectedCategory == "all" || pkg.type == selectedCategory) &&
            (searchQuery.isBlank() || pkg.name.contains(searchQuery, ignoreCase = true) || pkg.description.contains(searchQuery, ignoreCase = true))
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Package Manager",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Main Tabs: Catalog vs Installed vs Logs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                Text("Catalog", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.SemiBold)
            }
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                Text("Installed (${installedPackages.size})", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.SemiBold)
            }
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) {
                Text("Logs (${logs.size})", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.SemiBold)
            }
        }

        when (selectedTab) {
            0 -> {
                // Catalog View
                // Filter Categories
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedCategory == "all",
                        onClick = { selectedCategory = "all" },
                        label = { Text("All") }
                    )
                    FilterChip(
                        selected = selectedCategory == "sys",
                        onClick = { selectedCategory = "sys" },
                        label = { Text("System / CLI") }
                    )
                    FilterChip(
                        selected = selectedCategory == "pip",
                        onClick = { selectedCategory = "pip" },
                        label = { Text("Python Pip") }
                    )
                    FilterChip(
                        selected = selectedCategory == "npm",
                        onClick = { selectedCategory = "npm" },
                        label = { Text("Node.js npm") }
                    )
                }

                // Search Box
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search packages...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("package_search_input")
                )

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(availableList) { pkg ->
                        val isInstalled = installedPackages.any { it.id == pkg.id }
                        GlassCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = pkg.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "v${pkg.version}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "• ${pkg.type.uppercase()}",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = pkg.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Size: ${pkg.downloadSize}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }

                                if (isInstalled) {
                                    OutlinedButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                packageService.removePackage(pkg.id) { addLog(it) }
                                                Toast.makeText(context, "Package removed", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Text("Remove", fontSize = 12.sp)
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            coroutineScope.launch {
                                                isInstalling = true
                                                selectedTab = 2 // switch to logs
                                                val res = packageService.installPackage(pkg) { addLog(it) }
                                                isInstalling = false
                                                if (res.isSuccess) {
                                                    Toast.makeText(context, "✓ Installed ${pkg.name}", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                        ),
                                        modifier = Modifier.testTag("install_pkg_${pkg.name}")
                                    ) {
                                        Text("Install", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // Installed Packages
                if (installedPackages.isEmpty()) {
                    GlassCard(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "No packages installed yet. Select from the Catalog tab or use 'nex pkg install <name>' in terminal.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(installedPackages) { pkg ->
                            GlassCard {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(pkg.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                        Text("Version: ${pkg.version} • Type: ${pkg.type.uppercase()}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                        Text(pkg.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    IconButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                packageService.removePackage(pkg.id) { addLog(it) }
                                                Toast.makeText(context, "Removed ${pkg.name}", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // Logs View
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
                            Text("No package operation logs recorded.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
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
                                    fontSize = 12.sp,
                                    color = if (log.startsWith("✓")) MaterialTheme.colorScheme.primary else if (log.startsWith("✗")) MaterialTheme.colorScheme.error else TextPrimaryDark
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
