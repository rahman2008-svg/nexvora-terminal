package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.ui.theme.AccentChoice
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "nexvora_settings")

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.DARK,
    val accentChoice: AccentChoice = AccentChoice.CYAN,
    val terminalFontSize: Int = 13,
    val editorFontSize: Int = 14,
    val terminalFont: String = "Monospace",
    val editorFont: String = "Monospace",
    val cursorStyle: String = "BLOCK", // BLOCK, BEAM, UNDERLINE
    val autoSave: Boolean = true,
    val wordWrap: Boolean = false,
    val tabSize: Int = 4,
    val historySize: Int = 2000,
    val bellVibration: Boolean = true,
    val firstRunCompleted: Boolean = false
)

class SettingsDataStore(private val context: Context) {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val ACCENT_CHOICE = stringPreferencesKey("accent_choice")
        val TERMINAL_FONT_SIZE = intPreferencesKey("terminal_font_size")
        val EDITOR_FONT_SIZE = intPreferencesKey("editor_font_size")
        val TERMINAL_FONT = stringPreferencesKey("terminal_font")
        val EDITOR_FONT = stringPreferencesKey("editor_font")
        val CURSOR_STYLE = stringPreferencesKey("cursor_style")
        val AUTO_SAVE = booleanPreferencesKey("auto_save")
        val WORD_WRAP = booleanPreferencesKey("word_wrap")
        val TAB_SIZE = intPreferencesKey("tab_size")
        val HISTORY_SIZE = intPreferencesKey("history_size")
        val BELL_VIBRATION = booleanPreferencesKey("bell_vibration")
        val FIRST_RUN_COMPLETED = booleanPreferencesKey("first_run_completed")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { preferences ->
        val themeModeStr = preferences[PreferencesKeys.THEME_MODE] ?: ThemeMode.DARK.name
        val accentStr = preferences[PreferencesKeys.ACCENT_CHOICE] ?: AccentChoice.CYAN.name

        AppSettings(
            themeMode = try { ThemeMode.valueOf(themeModeStr) } catch (_: Exception) { ThemeMode.DARK },
            accentChoice = try { AccentChoice.valueOf(accentStr) } catch (_: Exception) { AccentChoice.CYAN },
            terminalFontSize = preferences[PreferencesKeys.TERMINAL_FONT_SIZE] ?: 13,
            editorFontSize = preferences[PreferencesKeys.EDITOR_FONT_SIZE] ?: 14,
            terminalFont = preferences[PreferencesKeys.TERMINAL_FONT] ?: "Monospace",
            editorFont = preferences[PreferencesKeys.EDITOR_FONT] ?: "Monospace",
            cursorStyle = preferences[PreferencesKeys.CURSOR_STYLE] ?: "BLOCK",
            autoSave = preferences[PreferencesKeys.AUTO_SAVE] ?: true,
            wordWrap = preferences[PreferencesKeys.WORD_WRAP] ?: false,
            tabSize = preferences[PreferencesKeys.TAB_SIZE] ?: 4,
            historySize = preferences[PreferencesKeys.HISTORY_SIZE] ?: 2000,
            bellVibration = preferences[PreferencesKeys.BELL_VIBRATION] ?: true,
            firstRunCompleted = preferences[PreferencesKeys.FIRST_RUN_COMPLETED] ?: false
        )
    }

    suspend fun updateThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[PreferencesKeys.THEME_MODE] = mode.name }
    }

    suspend fun updateAccentChoice(accent: AccentChoice) {
        context.dataStore.edit { it[PreferencesKeys.ACCENT_CHOICE] = accent.name }
    }

    suspend fun updateTerminalFontSize(size: Int) {
        context.dataStore.edit { it[PreferencesKeys.TERMINAL_FONT_SIZE] = size }
    }

    suspend fun updateEditorFontSize(size: Int) {
        context.dataStore.edit { it[PreferencesKeys.EDITOR_FONT_SIZE] = size }
    }

    suspend fun updateAutoSave(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.AUTO_SAVE] = enabled }
    }

    suspend fun updateWordWrap(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.WORD_WRAP] = enabled }
    }

    suspend fun updateTabSize(size: Int) {
        context.dataStore.edit { it[PreferencesKeys.TAB_SIZE] = size }
    }

    suspend fun updateCursorStyle(style: String) {
        context.dataStore.edit { it[PreferencesKeys.CURSOR_STYLE] = style }
    }

    suspend fun updateBellVibration(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.BELL_VIBRATION] = enabled }
    }

    suspend fun setFirstRunCompleted(completed: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.FIRST_RUN_COMPLETED] = completed }
    }

    suspend fun resetSettings() {
        context.dataStore.edit { it.clear() }
    }
}
