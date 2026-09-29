package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.WorkspaceManager
import com.example.files.FileItem
import com.example.files.FileManager
import com.example.files.FileSortOption
import com.example.files.ZipUtils
import com.example.ui.components.EmptyStateView
import com.example.ui.components.GlassCard
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilesScreen(
    workspaceManager: WorkspaceManager,
    onOpenFileInEditor: (File) -> Unit,
    onOpenTerminalHere: (File) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val fileManager = remember { FileManager() }

    var currentDir by remember { mutableStateOf(workspaceManager.rootDir) }
    var fileItems by remember { mutableStateOf<List<FileItem>>(emptyList()) }
    var sortOption by remember { mutableStateOf(FileSortOption.NAME_ASC) }
    var searchQuery by remember { mutableStateOf("") }

    // Dialog states
    var showCreateFileDialog by remember { mutableStateOf(false) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf<File?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf<File?>(null) }
    var showDetailsDialog by remember { mutableStateOf<File?>(null) }
    var showSortMenu by remember { mutableStateOf(false) }
    var selectedItemForAction by remember { mutableStateOf<FileItem?>(null) }

    fun refresh() {
        fileItems = fileManager.listFiles(currentDir, sortOption)
    }

    LaunchedEffect(currentDir, sortOption) {
        refresh()
    }

    // Handle back button inside directories
    BackHandler(enabled = currentDir != workspaceManager.rootDir) {
        currentDir.parentFile?.let {
            if (it.absolutePath.startsWith(workspaceManager.rootDir.absolutePath)) {
                currentDir = it
            } else {
                currentDir = workspaceManager.rootDir
            }
        }
    }

    val displayItems = remember(fileItems, searchQuery) {
        if (searchQuery.isBlank()) fileItems
        else fileItems.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Navigation bar / Breadcrumbs
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentDir != workspaceManager.rootDir) {
                    IconButton(
                        onClick = {
                            currentDir.parentFile?.let { currentDir = it }
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Go Up")
                    }
                }

                Text(
                    text = currentDir.absolutePath.removePrefix(workspaceManager.rootDir.parent ?: "").ifEmpty { "/" },
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // Sort Button
                IconButton(onClick = { showSortMenu = true }) {
                    Icon(Icons.Default.Sort, contentDescription = "Sort")
                }
                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Name (A to Z)") },
                        onClick = { sortOption = FileSortOption.NAME_ASC; showSortMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Name (Z to A)") },
                        onClick = { sortOption = FileSortOption.NAME_DESC; showSortMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Date (Newest first)") },
                        onClick = { sortOption = FileSortOption.DATE_DESC; showSortMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Size (Largest first)") },
                        onClick = { sortOption = FileSortOption.SIZE_DESC; showSortMenu = false }
                    )
                }

                // Add Menu
                IconButton(onClick = { showCreateFileDialog = true }) {
                    Icon(Icons.Default.NoteAdd, contentDescription = "New File")
                }
                IconButton(onClick = { showCreateFolderDialog = true }) {
                    Icon(Icons.Default.CreateNewFolder, contentDescription = "New Folder")
                }
            }
        }

        // Search Bar
        PaddingValues(horizontal = 16.dp, vertical = 6.dp)
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Filter files in current folder...", fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                    }
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .testTag("file_search_input")
        )

        // File List
        if (displayItems.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.FolderOpen,
                title = "Folder is Empty",
                description = "Create a file or subfolder to start organizing your code.",
                actionLabel = "Create File",
                onAction = { showCreateFileDialog = true },
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(displayItems) { item ->
                    FileRowItem(
                        item = item,
                        onClick = {
                            if (item.isDirectory) {
                                currentDir = item.file
                            } else {
                                onOpenFileInEditor(item.file)
                            }
                        },
                        onActionClick = {
                            selectedItemForAction = item
                        }
                    )
                }
            }
        }
    }

    // Action Bottom Sheet / Menu
    if (selectedItemForAction != null) {
        val item = selectedItemForAction!!
        ModalBottomSheet(
            onDismissRequest = { selectedItemForAction = null },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = item.file.path,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                if (!item.isDirectory) {
                    ActionMenuItem(Icons.Default.Code, "Open in Code Editor") {
                        selectedItemForAction = null
                        onOpenFileInEditor(item.file)
                    }
                }

                ActionMenuItem(Icons.Default.Terminal, "Open Terminal Here") {
                    selectedItemForAction = null
                    onOpenTerminalHere(if (item.isDirectory) item.file else item.file.parentFile ?: workspaceManager.rootDir)
                }

                ActionMenuItem(Icons.Default.Edit, "Rename") {
                    showRenameDialog = item.file
                    selectedItemForAction = null
                }

                ActionMenuItem(Icons.Default.CopyAll, "Duplicate") {
                    coroutineScope.launch {
                        fileManager.duplicate(item.file)
                        refresh()
                    }
                    selectedItemForAction = null
                }

                if (item.isDirectory) {
                    ActionMenuItem(Icons.Default.Archive, "Compress to ZIP") {
                        coroutineScope.launch {
                            val zipTarget = File(item.file.parentFile, "${item.name}.zip")
                            ZipUtils.zipDirectory(item.file, zipTarget)
                            refresh()
                        }
                        selectedItemForAction = null
                    }
                } else if (item.extension.lowercase() == "zip") {
                    ActionMenuItem(Icons.Default.Unarchive, "Extract ZIP Archive") {
                        coroutineScope.launch {
                            val extractDir = File(item.file.parentFile, item.name.removeSuffix(".zip"))
                            ZipUtils.unzip(item.file, extractDir)
                            refresh()
                        }
                        selectedItemForAction = null
                    }
                }

                ActionMenuItem(Icons.Default.Info, "Properties & Hash") {
                    showDetailsDialog = item.file
                    selectedItemForAction = null
                }

                ActionMenuItem(Icons.Default.Delete, "Delete", isDestructive = true) {
                    showDeleteConfirmDialog = item.file
                    selectedItemForAction = null
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Dialogs: Create File, Create Folder, Rename, Delete, Details
    if (showCreateFileDialog) {
        var newFileName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreateFileDialog = false },
            title = { Text("Create New File") },
            text = {
                OutlinedTextField(
                    value = newFileName,
                    onValueChange = { newFileName = it },
                    placeholder = { Text("e.g. main.py, script.sh, index.html") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("new_file_name_input")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFileName.isNotBlank()) {
                            coroutineScope.launch {
                                fileManager.createFile(currentDir, newFileName.trim())
                                refresh()
                            }
                        }
                        showCreateFileDialog = false
                    },
                    modifier = Modifier.testTag("confirm_create_file_btn")
                ) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = { showCreateFileDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showCreateFolderDialog) {
        var newFolderName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreateFolderDialog = false },
            title = { Text("Create New Directory") },
            text = {
                OutlinedTextField(
                    value = newFolderName,
                    onValueChange = { newFolderName = it },
                    placeholder = { Text("e.g. src, components, tests") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("new_folder_name_input")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFolderName.isNotBlank()) {
                            coroutineScope.launch {
                                fileManager.createDirectory(currentDir, newFolderName.trim())
                                refresh()
                            }
                        }
                        showCreateFolderDialog = false
                    },
                    modifier = Modifier.testTag("confirm_create_folder_btn")
                ) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = { showCreateFolderDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showRenameDialog != null) {
        val target = showRenameDialog!!
        var renameText by remember { mutableStateOf(target.name) }
        AlertDialog(
            onDismissRequest = { showRenameDialog = null },
            title = { Text("Rename") },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("rename_input")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (renameText.isNotBlank()) {
                            coroutineScope.launch {
                                fileManager.rename(target, renameText.trim())
                                refresh()
                            }
                        }
                        showRenameDialog = null
                    }
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = null }) { Text("Cancel") }
            }
        )
    }

    if (showDeleteConfirmDialog != null) {
        val target = showDeleteConfirmDialog!!
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = null },
            title = { Text("Confirm Deletion") },
            text = {
                Text(
                    if (target.isDirectory) "Are you sure you want to recursively delete '${target.name}' and all its files? This action cannot be undone."
                    else "Are you sure you want to delete '${target.name}'?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            fileManager.delete(target)
                            refresh()
                        }
                        showDeleteConfirmDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = null }) { Text("Cancel") }
            }
        )
    }

    if (showDetailsDialog != null) {
        val target = showDetailsDialog!!
        var sha256 by remember { mutableStateOf("Calculating...") }
        LaunchedEffect(target) {
            sha256 = fileManager.calculateHash(target, "SHA-256")
        }

        AlertDialog(
            onDismissRequest = { showDetailsDialog = null },
            title = { Text("File Information") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Name: ${target.name}", fontWeight = FontWeight.Bold)
                    Text("Type: ${if (target.isDirectory) "Directory" else "File (${target.extension})"}")
                    Text("Size: ${if (target.isDirectory) "${target.listFiles()?.size ?: 0} items" else "${target.length()} bytes"}")
                    Text("Path: ${target.absolutePath}", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    Text("Last Modified: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(target.lastModified()))}")
                    Text("SHA-256:", fontWeight = FontWeight.SemiBold)
                    Text(sha256, fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.primary)
                }
            },
            confirmButton = {
                TextButton(onClick = { showDetailsDialog = null }) { Text("Close") }
            }
        )
    }
}

@Composable
private fun FileRowItem(
    item: FileItem,
    onClick: () -> Unit,
    onActionClick: () -> Unit
) {
    GlassCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().testTag("file_row_${item.name}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (item.isDirectory) Icons.Default.Folder else Icons.Default.InsertDriveFile,
                contentDescription = null,
                tint = if (item.isDirectory) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (item.isDirectory) "${item.size} items" else "${item.size} B • ${SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(item.lastModified))}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onActionClick) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = "Options",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ActionMenuItem(
    icon: ImageVector,
    label: String,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
    }
}
