# NexVora Terminal

> **Code • Run • Build • Anywhere**

**Developer:** Prince AR Abdur Rahman  
**Publisher:** NexVora Lab’s Ofc  
**Version:** 1.0.0 (Production Release)  
**Platform:** Android Subsystem (AOSP) • Jetpack Compose • Material 3  

---

## ⚡ Overview

**NexVora Terminal** is a modern, Android-first, Termux-style mobile development workstation that combines a real terminal emulator, code editor with syntax highlighting, project manager, file manager, package manager, Git/GitHub tools, build tools, and developer utilities in one cohesive, offline-first application.

Designed with a futuristic Obsidian developer UI:
- Deep dark and pure AMOLED black backgrounds
- Electric Cyan primary accent & Neon Violet secondary accent
- Glass-like cards with subtle borders
- Monospaced typography for code & terminal
- Horizontal scroll extra-key row (CTRL, ALT, TAB, ESC, Arrows, Code brackets)
- Completely offline-first with zero requirements for Firebase, Supabase, or paid cloud APIs

---

## 🛠️ System Architecture

- **Subsystem Terminal:** Native `/system/bin/sh` process execution with ANSI color parsing, multiple concurrent sessions, history navigation (`↑`/`↓`), and custom `nex` CLI command interceptor.
- **Mobile Code Editor:** Multi-tab file editing, token-level syntax highlighting (Python, JavaScript, TypeScript, C/C++, Java, Kotlin, HTML, CSS, SQL, Bash), line numbers, search & replace, undo/redo stack.
- **App-Managed Workspace:** Isolated file structure:
  ```text
  NexVora/
  ├── projects/
  ├── downloads/
  ├── scripts/
  ├── packages/
  └── workspace/
  ```
- **Local HTTP Web Server:** Built-in socket server running on `127.0.0.1:8080` to serve static sites and preview web projects in the browser.
- **Git & GitHub Integration:** Local repository initialization, branch switching, commits log, diff viewer, and HTTPS cloning with secure user token storage.
- **Unified Package Manager:** Local package management for Linux/system utilities, Python (`pip`), and Node.js (`npm`).
- **Build Engine:** Automatic project type detection with real compiler and interpreter execution, live stream logs, and artifact tracking.
- **Developer Utilities:** JSON formatter, Base64 encoder/decoder, URL encoder/decoder, Regex tester, UUID v4 generator, Hash generator (MD5, SHA-1, SHA-256), Unix timestamp converter, case converter, and HTTP request tester.

---

## 💻 Built-in `nex` CLI Commands

Run directly inside the terminal:

| Command | Description |
|---|---|
| `nex help` | Displays the command palette and usage guide |
| `nex doctor` | Runs real diagnostics on storage, shell, git, and runtimes |
| `nex new <project> [--type]` | Scaffolds a new project (`python`, `web`, `c`, `node`, `bash`) |
| `nex run <file>` | Executes script with auto-detected interpreter |
| `nex open <file>` | Opens the file directly in the mobile code editor |
| `nex projects` | Lists all projects in the workspace |
| `nex build` | Triggers the build routine for the current project |
| `nex pkg install <package>` | Installs a system package into the sandbox |
| `nex pkg remove <package>` | Removes an installed package |
| `nex pip install <package>` | Registers a Python pip package |
| `nex npm install <package>` | Registers a Node.js npm dependency |
| `nex git status` | Displays status of the repository |
| `nex git clone <url>` | Clones a remote repository |

---

## 🛡️ Privacy & Security

- Strictly respects the Android App Sandbox and Scoped Storage.
- No embedded secrets or plaintext credential storage.
- Operates primarily offline without transmitting user code to third-party servers.
