package com.smarthome.monitor.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.smarthome.monitor.core.util.Constants
import com.smarthome.monitor.domain.repository.AuthRepository
import com.smarthome.monitor.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isLoading: Boolean = true,
    val userName: String = "Videesha",
    val notificationsEnabled: Boolean = true,
    val ironLimitMinutes: Int = 15,
    val isLoggingOut: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val loggingOutFlow = MutableStateFlow(false)

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.notificationsEnabled,
        settingsRepository.ironLimitMinutes,
        settingsRepository.userName,
        loggingOutFlow
    ) { notificationsEnabled, ironLimitMinutes, userName, isLoggingOut ->
        SettingsUiState(
            isLoading = false,
            userName = userName,
            notificationsEnabled = notificationsEnabled,
            ironLimitMinutes = ironLimitMinutes,
            isLoggingOut = isLoggingOut
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setNotificationsEnabled(enabled)
            try {
                val topic = "home_${Constants.HOME_ID}"
                if (enabled) {
                    FirebaseMessaging.getInstance().subscribeToTopic(topic)
                } else {
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setIronLimitMinutes(minutes: Int) {
        viewModelScope.launch {
            settingsRepository.setIronLimitMinutes(minutes)
        }
    }

    fun setUserName(name: String) {
        viewModelScope.launch {
            settingsRepository.setUserName(name)
        }
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            loggingOutFlow.value = true
            try {
                authRepository.signOut()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                loggingOutFlow.value = false
                onComplete()
            }
        }
    }
}
