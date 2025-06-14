package com.example.fung_eye

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SettingsViewModel : ViewModel() {
    // Private mutable state for the theme
    private val _isDarkTheme = MutableStateFlow(false)
    // Public immutable state flow to observe in the UI
    val isDarkTheme = _isDarkTheme.asStateFlow()

    /**
     * Toggles the current theme between light and dark mode.
     */
    fun toggleTheme() {
        _isDarkTheme.update { !it }
    }
}
