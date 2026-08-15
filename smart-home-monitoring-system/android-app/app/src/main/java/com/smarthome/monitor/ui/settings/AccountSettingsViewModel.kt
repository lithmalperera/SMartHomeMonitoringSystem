package com.smarthome.monitor.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarthome.monitor.core.util.Constants
import com.smarthome.monitor.domain.repository.AuthRepository
import com.smarthome.monitor.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AccountSettingsUiState(
    val userName: String = "Videesha",
    val userId: String = "—",
    val homeId: String = Constants.HOME_ID,
    val homeName: String = "Hestia",
    val signInMethod: String = "Anonymous",
    val appVersion: String = "v2.4.8"
)

@HiltViewModel
class AccountSettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AccountSettingsUiState(userId = authRepository.currentUserId ?: "—")
    )
    val uiState: StateFlow<AccountSettingsUiState> = _uiState

    init {
        viewModelScope.launch {
            val name = settingsRepository.userName.first()
            _uiState.value = _uiState.value.copy(userName = name)
        }
    }

    fun setUserName(name: String) {
        viewModelScope.launch {
            settingsRepository.setUserName(name)
            _uiState.value = _uiState.value.copy(userName = name)
        }
    }
}
