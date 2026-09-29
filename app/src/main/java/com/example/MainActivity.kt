package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.buildsystem.BuildEngine
import com.example.core.LocalHttpServer
import com.example.core.NexCommandDispatcher
import com.example.core.WorkspaceManager
import com.example.data.*
import com.example.editor.EditorTab
import com.example.git.GitEngine
import com.example.packages.PackageManagerService
import com.example.projects.ProjectManager
import com.example.runtime.RuntimeManager
import com.example.terminal.TerminalSession
import com.example.ui.components.CommandPaletteDialog
import com.example.ui.components.OnboardingDialog
import com.example.ui.components.PaletteAction
import com.example.ui.screens.*
import com.example.ui.theme.NexVoraTheme
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : ComponentActivity() {

    private lateinit var database: AppDatabase
    private lateinit var workspaceManager: WorkspaceManager
    private lateinit var settingsDataStore: SettingsDataStore
    private lateinit var commandDispatcher: NexCommandDispatcher
    private lateinit var httpServer: LocalHttpServer
    private lateinit var buildEngine: BuildEngine
    private lateinit var packageService: PackageManagerService
    private lateinit var gitEngine: GitEngine
    private lateinit var projectManager: ProjectManager
    private lateinit var runtimeManager: RuntimeManager

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        database = AppDatabase.getDatabase(this)
        workspaceManager = WorkspaceManager(this)
        settingsDataStore = SettingsDataStore(this)
        httpServer = LocalHttpServer()
        buildEngine = BuildEngine()
        packageService = PackageManagerService(workspaceManager, database)
        gitEngine = GitEngine()
        projectManager = ProjectManager(workspaceManager, database)
        runtimeManager = RuntimeManager()

        setContent {
            val coroutineScope = rememberCoroutineScope()
            val settings by settingsDataStore.settingsFlow.collectAsState(initial = AppSettings())

            // Initialize workspace and demo projects on launch
            LaunchedEffect(Unit) {
                workspaceManager.initializeWorkspace { name, path, type ->
                    coroutineScope.launch {
                        database.appDao().insertProject(
                            ProjectEntity(
                                name = name,
                                path = path,
                                type = type,
                                description = "Demo $type project",
                                gitInitialized = true
                            )
                        )
                    }
                }
            }

            // Room reactive flows
            val projects by database.appDao().getAllProjects().collectAsState(initial = emptyList())
            val recentFiles by database.appDao().getRecentFiles().collectAsState(initial = emptyList())
            val installedPackages by database.appDao().getInstalledPackages().collectAsState(initial = emptyList())

            // Navigation state
            var currentTab by remember { mutableIntStateOf(0) } // 0: Home, 1: Files, 2: Terminal, 3: Editor, 4: More
            var moreSubscreen by remember { mutableStateOf<String?>(null) }
            var isCommandPaletteOpen by remember { mutableStateOf(false) }

            // Terminal sessions
            val terminalSessions = remember {
                mutableStateListOf(
                    TerminalSession(
                        title = "Terminal 1",
                        workingDir = workspaceManager.workspaceDir,
                        dispatcher = NexCommandDispatcher(workspaceManager, database) { file ->
                            // Open file in editor callback
                        }
                    )
                )
            }
            var activeTerminalIndex by remember { mutableIntStateOf(0) }

            // Editor tabs
            val editorTabs = remember { mutableStateListOf<EditorTab>() }
            var activeEditorTabIndex by remember { mutableIntStateOf(0) }

            fun openFileInEditor(file: File) {
                val existingIndex = editorTabs.indexOfFirst { it.file.absolutePath == file.absolutePath }
                if (existingIndex >= 0) {
                    activeEditorTabIndex = existingIndex
                } else {
                    val content = try { file.readText() } catch (_: Exception) { "" }
                    editorTabs.add(EditorTab(file = file, content = content))
                    activeEditorTabIndex = editorTabs.size - 1
                }
                currentTab = 3 // Switch to Editor tab
                coroutineScope.launch {
                    database.appDao().insertRecentFile(
                        RecentFileEntity(
                            path = file.absolutePath,
                            name = file.name,
                            language = EditorTab.detectLanguage(file.name)
                        )
                    )
                }
            }

            fun openTerminalAt(dir: File) {
                val newSession = TerminalSession(
                    title = "Term ${terminalSessions.size + 1}",
                    workingDir = dir,
                    dispatcher = NexCommandDispatcher(workspaceManager, database) { openFileInEditor(it) }
                )
                terminalSessions.add(newSession)
                activeTerminalIndex = terminalSessions.size - 1
                currentTab = 2 // Switch to Terminal tab
            }

            // Command palette actions
            val paletteActions = remember(projects) {
                listOf(
                    PaletteAction("New Project", "Create application from templates", "PROJECTS", Icons.Default.Add) {
                        currentTab = 4; moreSubscreen = "projects"
                    },
                    PaletteAction("Terminal", "Switch to interactive shell", "TERMINAL", Icons.Default.Terminal) {
                        currentTab = 2
                    },
                    PaletteAction("Code Editor", "Open mobile code workspace", "EDITOR", Icons.Default.Code) {
                        currentTab = 3
                    },
                    PaletteAction("File Manager", "Explore NexVora workspace directories", "FILES", Icons.Default.Folder) {
                        currentTab = 1
                    },
                    PaletteAction("Package Manager", "Install Python, npm, or system packages", "PACKAGES", Icons.Default.Memory) {
                        currentTab = 4; moreSubscreen = "packages"
                    },
                    PaletteAction("Git & GitHub", "Commit, push, pull, or clone repository", "GIT", Icons.Default.Commit) {
                        currentTab = 4; moreSubscreen = "git"
                    },
                    PaletteAction("Build Project", "Compile or execute project entry point", "BUILD", Icons.Default.PlayArrow) {
                        currentTab = 4; moreSubscreen = "build"
                    },
                    PaletteAction("Local HTTP Server", "Host web apps on 127.0.0.1:8080", "SERVER", Icons.Default.Sensors) {
                        currentTab = 4; moreSubscreen = "server"
                    },
                    PaletteAction("Developer Tools", "JSON, Base64, Regex, Hash utilities", "TOOLS", Icons.Default.Build) {
                        currentTab = 4; moreSubscreen = "devtools"
                    },
                    PaletteAction("Settings", "Appearance, fonts, AMOLED mode", "SYSTEM", Icons.Default.Settings) {
                        currentTab = 4; moreSubscreen = "settings"
                    },
                    PaletteAction("Run Doctor", "Execute nex doctor system check", "CLI", Icons.Default.MedicalServices) {
                        currentTab = 2
                        terminalSessions.getOrNull(activeTerminalIndex)?.execute("nex doctor")
                    }
                ) + projects.map { proj ->
                    PaletteAction("Open ${proj.name}", "Project (${proj.type})", "PROJECT", Icons.Default.FolderOpen) {
                        currentTab = 1
                    }
                }
            }

            // Back handler logic
            BackHandler(enabled = moreSubscreen != null || currentTab != 0) {
                if (moreSubscreen != null) {
                    moreSubscreen = null
                } else if (currentTab != 0) {
                    currentTab = 0
                }
            }

            NexVoraTheme(
                themeMode = settings.themeMode,
                accent = settings.accentChoice
            ) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = {
                                Column {
                                    Text(
                                        text = when (currentTab) {
                                            0 -> "NexVora Terminal"
                                            1 -> "Files"
                                            2 -> "Terminal"
                                            3 -> "Code Editor"
                                            4 -> when (moreSubscreen) {
                                                "projects" -> "Projects"
                                                "packages" -> "Packages"
                                                "git" -> "Git & GitHub"
                                                "build" -> "Build & Run"
                                                "runtimes" -> "Runtimes"
                                                "server" -> "Local Server"
                                                "devtools" -> "Developer Tools"
                                                "settings" -> "Settings"
                                                "about" -> "About NexVora"
                                                else -> "Workstation Suite"
                                            }
                                            else -> "NexVora"
                                        },
                                        fontSize = 18.sp,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                    )
                                    Text(
                                        text = "Code • Run • Build • Anywhere",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            navigationIcon = {
                                if (currentTab == 4 && moreSubscreen != null) {
                                    IconButton(onClick = { moreSubscreen = null }) {
                                        Icon(Icons.Default.ArrowBack, contentDescription = "Back to More")
                                    }
                                }
                            },
                            actions = {
                                IconButton(
                                    onClick = { isCommandPaletteOpen = true },
                                    modifier = Modifier.testTag("global_search_icon_btn")
                                ) {
                                    Icon(
                                        Icons.Default.Search,
                                        contentDescription = "Search & Commands",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                titleContentColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 4.dp
                        ) {
                            val navItems = listOf(
                                Triple(0, "Home", Icons.Default.Home),
                                Triple(1, "Files", Icons.Default.Folder),
                                Triple(2, "Terminal", Icons.Default.Terminal),
                                Triple(3, "Editor", Icons.Default.Code),
                                Triple(4, "More", Icons.Default.Menu)
                            )
                            navItems.forEach { (index, title, icon) ->
                                NavigationBarItem(
                                    selected = currentTab == index,
                                    onClick = {
                                        currentTab = index
                                        if (index != 4) moreSubscreen = null
                                    },
                                    icon = { Icon(icon, contentDescription = title) },
                                    label = { Text(title, fontSize = 11.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                    ),
                                    modifier = Modifier.testTag("nav_tab_$title")
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentTab) {
                            0 -> HomeScreen(
                                workspaceManager = workspaceManager,
                                projects = projects,
                                recentFiles = recentFiles,
                                terminalSessionCount = terminalSessions.size,
                                storageStats = workspaceManager.getStorageStats(),
                                onNavigateToTab = { currentTab = it },
                                onNavigateToMoreSubscreen = {
                                    currentTab = 4
                                    moreSubscreen = it
                                },
                                onOpenProject = { proj ->
                                    openTerminalAt(File(proj.path))
                                },
                                onOpenFile = { file ->
                                    openFileInEditor(file)
                                },
                                onOpenCommandPalette = { isCommandPaletteOpen = true }
                            )

                            1 -> FilesScreen(
                                workspaceManager = workspaceManager,
                                onOpenFileInEditor = { openFileInEditor(it) },
                                onOpenTerminalHere = { openTerminalAt(it) }
                            )

                            2 -> TerminalScreen(
                                sessions = terminalSessions,
                                activeSessionIndex = activeTerminalIndex,
                                onSelectSession = { activeTerminalIndex = it },
                                onAddSession = {
                                    val newSession = TerminalSession(
                                        title = "Terminal ${terminalSessions.size + 1}",
                                        workingDir = workspaceManager.workspaceDir,
                                        dispatcher = NexCommandDispatcher(workspaceManager, database) { openFileInEditor(it) }
                                    )
                                    terminalSessions.add(newSession)
                                    activeTerminalIndex = terminalSessions.size - 1
                                },
                                onCloseSession = { index ->
                                    if (terminalSessions.size > 1) {
                                        val removed = terminalSessions.removeAt(index)
                                        removed.destroy()
                                        if (activeTerminalIndex >= terminalSessions.size) {
                                            activeTerminalIndex = terminalSessions.size - 1
                                        }
                                    }
                                },
                                terminalFontSize = settings.terminalFontSize
                            )

                            3 -> EditorScreen(
                                tabs = editorTabs,
                                activeTabIndex = activeEditorTabIndex,
                                onSelectTab = { activeEditorTabIndex = it },
                                onCloseTab = { index ->
                                    editorTabs.removeAt(index)
                                    if (activeEditorTabIndex >= editorTabs.size) {
                                        activeEditorTabIndex = (editorTabs.size - 1).coerceAtLeast(0)
                                    }
                                },
                                onOpenFilesRequested = { currentTab = 1 },
                                editorFontSize = settings.editorFontSize,
                                wordWrap = settings.wordWrap
                            )

                            4 -> {
                                when (moreSubscreen) {
                                    "projects" -> ProjectsScreen(
                                        projects = projects,
                                        projectManager = projectManager,
                                        onOpenProjectFiles = { proj ->
                                            currentTab = 1
                                        },
                                        onOpenProjectTerminal = { proj ->
                                            openTerminalAt(File(proj.path))
                                        }
                                    )

                                    "packages" -> PackagesScreen(
                                        packageService = packageService,
                                        installedPackages = installedPackages
                                    )

                                    "git" -> GitScreen(
                                        projects = projects,
                                        gitEngine = gitEngine
                                    )

                                    "build" -> BuildScreen(
                                        projects = projects,
                                        buildEngine = buildEngine
                                    )

                                    "runtimes" -> RuntimeScreen(
                                        runtimeManager = runtimeManager
                                    )

                                    "server" -> LocalServerScreen(
                                        server = httpServer,
                                        workspaceManager = workspaceManager,
                                        projects = projects
                                    )

                                    "devtools" -> DevToolsScreen()

                                    "settings" -> SettingsScreen(
                                        settings = settings,
                                        settingsDataStore = settingsDataStore,
                                        workspaceManager = workspaceManager
                                    )

                                    "about" -> AboutScreen()

                                    else -> MoreScreen(
                                        onNavigateToSubscreen = { sub -> moreSubscreen = sub }
                                    )
                                }
                            }
                        }
                    }
                }

                // Global Command Palette & Search Dialog
                CommandPaletteDialog(
                    isOpen = isCommandPaletteOpen,
                    onDismiss = { isCommandPaletteOpen = false },
                    actions = paletteActions
                )

                // First Run Onboarding Dialog
                OnboardingDialog(
                    isOpen = !settings.firstRunCompleted,
                    onComplete = {
                        coroutineScope.launch {
                            settingsDataStore.setFirstRunCompleted(true)
                        }
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        httpServer.stop()
    }
}
