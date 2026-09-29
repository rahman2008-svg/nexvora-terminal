package com.example.projects

import com.example.core.WorkspaceManager
import com.example.data.AppDatabase
import com.example.data.ProjectEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

enum class ProjectType(val label: String, val icon: String, val ext: String) {
    EMPTY("Empty Project", "📁", "txt"),
    PYTHON("Python", "🐍", "py"),
    NODEJS("Node.js", "🟢", "js"),
    C("C Program", "⚙️", "c"),
    CPP("C++ Program", "⚙️", "cpp"),
    JAVA("Java", "☕", "java"),
    KOTLIN("Kotlin", "🟣", "kt"),
    WEB("Web (HTML/CSS/JS)", "🌐", "html"),
    BASH("Bash Script", "🐚", "sh"),
    CUSTOM("Custom", "📦", "")
}

class ProjectManager(
    private val workspaceManager: WorkspaceManager,
    private val database: AppDatabase
) {

    suspend fun createProject(
        name: String,
        type: ProjectType,
        initGit: Boolean = true,
        generateReadme: Boolean = true,
        generateGitignore: Boolean = true
    ): Result<ProjectEntity> = withContext(Dispatchers.IO) {
        try {
            val projectDir = File(workspaceManager.projectsDir, name)
            if (projectDir.exists()) {
                return@withContext Result.failure(Exception("Project folder '$name' already exists."))
            }
            projectDir.mkdirs()

            // Generate template files
            when (type) {
                ProjectType.PYTHON -> {
                    File(projectDir, "main.py").writeText(
                        """# $name
# Created with NexVora Terminal

def main():
    print("Running $name on NexVora Android Subsystem!")

if __name__ == "__main__":
    main()
""".trimIndent()
                    )
                    File(projectDir, "requirements.txt").writeText("# Add dependencies here\n")
                }
                ProjectType.NODEJS -> {
                    File(projectDir, "index.js").writeText(
                        """// $name
console.log("Welcome to $name on NexVora!");
""".trimIndent()
                    )
                    File(projectDir, "package.json").writeText(
                        """{
  "name": "${name.lowercase().replace(" ", "-")}",
  "version": "1.0.0",
  "description": "NexVora mobile project",
  "main": "index.js",
  "scripts": {
    "start": "node index.js"
  }
}
""".trimIndent()
                    )
                }
                ProjectType.C -> {
                    File(projectDir, "main.c").writeText(
                        """#include <stdio.h>

int main(int argc, char *argv[]) {
    printf("Hello from %s!\n", "$name");
    return 0;
}
""".trimIndent()
                    )
                    File(projectDir, "Makefile").writeText(
                        """CC = gcc
CFLAGS = -Wall -O2

all: main

main: main.c
	${'$'}(CC) ${'$'}(CFLAGS) -o main main.c

clean:
	rm -f main
""".trimIndent()
                    )
                }
                ProjectType.CPP -> {
                    File(projectDir, "main.cpp").writeText(
                        """#include <iostream>

int main() {
    std::cout << "Hello from $name C++ project!" << std::endl;
    return 0;
}
""".trimIndent()
                    )
                    File(projectDir, "Makefile").writeText(
                        """CXX = g++
CXXFLAGS = -Wall -std=c++17

all: main

main: main.cpp
	${'$'}(CXX) ${'$'}(CXXFLAGS) -o main main.cpp

clean:
	rm -f main
""".trimIndent()
                    )
                }
                ProjectType.JAVA -> {
                    File(projectDir, "Main.java").writeText(
                        """public class Main {
    public static void main(String[] args) {
        System.out.println("Hello from $name in Java!");
    }
}
""".trimIndent()
                    )
                }
                ProjectType.KOTLIN -> {
                    File(projectDir, "Main.kt").writeText(
                        """fun main() {
    println("Hello from $name in Kotlin!")
}
""".trimIndent()
                    )
                }
                ProjectType.WEB -> {
                    File(projectDir, "index.html").writeText(
                        """<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>$name</title>
  <link rel="stylesheet" href="style.css">
</head>
<body>
  <h1>$name</h1>
  <p>Static web project ready for local preview.</p>
  <script src="script.js"></script>
</body>
</html>
""".trimIndent()
                    )
                    File(projectDir, "style.css").writeText(
                        """body {
  background: #0A0E17;
  color: #00E5FF;
  font-family: sans-serif;
  text-align: center;
  padding: 50px;
}
""".trimIndent()
                    )
                    File(projectDir, "script.js").writeText(
                        """console.log("$name web app running");
""".trimIndent()
                    )
                }
                ProjectType.BASH -> {
                    File(projectDir, "script.sh").writeText(
                        """#!/system/bin/sh
echo "=== Running $name ==="
echo "Executed at: $(date)"
""".trimIndent()
                    )
                }
                else -> {
                    File(projectDir, "notes.txt").writeText("Notes for $name\n")
                }
            }

            // Project metadata file
            File(projectDir, "project.json").writeText(
                """{
  "name": "$name",
  "type": "${type.name.lowercase()}",
  "version": "1.0.0",
  "creator": "Prince AR Abdur Rahman",
  "publisher": "NexVora Lab's Ofc",
  "created": ${System.currentTimeMillis()}
}
""".trimIndent()
            )

            // README
            if (generateReadme) {
                File(projectDir, "README.md").writeText(
                    """# $name
> Code • Run • Build • Anywhere

**Type:** ${type.label}  
**Created:** ${java.util.Date()}  

### Getting Started
1. Edit code in the Editor tab.
2. Run via Terminal or the Build tab.
""".trimIndent()
                )
            }

            // .gitignore
            if (generateGitignore) {
                val gitignoreContent = when (type) {
                    ProjectType.PYTHON -> "__pycache__/\n*.pyc\n.venv/\n*.egg-info/\n"
                    ProjectType.NODEJS -> "node_modules/\n*.log\n.env\n"
                    ProjectType.C, ProjectType.CPP -> "*.o\n*.exe\nmain\nout\n"
                    ProjectType.JAVA -> "*.class\n*.jar\n"
                    else -> ".DS_Store\n*.tmp\n"
                }
                File(projectDir, ".gitignore").writeText(gitignoreContent)
            }

            // Git init
            if (initGit) {
                val gitDir = File(projectDir, ".git")
                gitDir.mkdirs()
                File(gitDir, "HEAD").writeText("ref: refs/heads/main\n")
            }

            val entity = ProjectEntity(
                name = name,
                path = projectDir.absolutePath,
                type = type.name.lowercase(),
                description = "${type.label} application",
                gitInitialized = initGit
            )
            val id = database.appDao().insertProject(entity)
            Result.success(entity.copy(id = id))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteProject(project: ProjectEntity): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val dir = File(project.path)
            if (dir.exists()) dir.deleteRecursively()
            database.appDao().deleteProject(project)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
