package com.smarthome.monitor.ui.settings

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class SettingsUiState(
    val isLoading: Boolean = true,
    val homeName: String = "Home 001",
    val notificationsEnabled: Boolean = true,
    val defaultSafetyLimit: Int = 30,
    val saved: Boolean = false
)

class SettingsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState

    init {
        _uiState.value = SettingsUiState(isLoading = false)
    }

    fun setHomeName(name: String) {
        _uiState.value = uiState.value.copy(homeName = name, saved = false)
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        _uiState.value = uiState.value.copy(notificationsEnabled = enabled, saved = false)
    }

    fun setSafetyLimit(limit: Int) {
        _uiState.value = uiState.value.copy(defaultSafetyLimit = limit, saved = false)
    }

    fun save() {
        _uiState.value = uiState.value.copy(saved = true)
    }

    fun resetSavedFlag() {
        _uiState.value = uiState.value.copy(saved = false)
    }
}