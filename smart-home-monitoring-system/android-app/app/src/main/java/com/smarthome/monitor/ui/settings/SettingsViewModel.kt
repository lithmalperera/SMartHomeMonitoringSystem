package com.smarthome.monitor.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import com.smarthome.monitor.core.util.Constants
import com.smarthome.monitor.domain.repository.AuthRepository
import com.smarthome.monitor.domain.repository.DeviceRepository
import com.smarthome.monitor.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isLoading: Boolean = true,
    val userName: String = "Videesha",
    val notificationsEnabled: Boolean = true,
    val ironLimitMinutes: Int = 15,
    val isLoggingOut: Boolean = false,
    val isOnline: Boolean = true,
    val isSyncing: Boolean = false,
    val lastSyncText: String = "—"
)

private data class SyncState(
    val isOnline: Boolean = true,
    val isSyncing: Boolean = false,
    val lastSyncText: String = "—"
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val authRepository: AuthRepository,
    private val deviceRepository: DeviceRepository,
    private val db: FirebaseDatabase
) : ViewModel() {

    private val loggingOutFlow = MutableStateFlow(false)
    private val syncFlow = MutableStateFlow(SyncState())

    init {
        db.getReference(".info/connected").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val online = snapshot.getValue(Boolean::class.java) ?: false
                syncFlow.value = syncFlow.value.copy(isOnline = online)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.notificationsEnabled,
        settingsRepository.ironLimitMinutes,
        settingsRepository.userName,
        loggingOutFlow,
        syncFlow
    ) { notificationsEnabled, ironLimitMinutes, userName, isLoggingOut, sync ->
        SettingsUiState(
            isLoading = false,
            userName = userName,
            notificationsEnabled = notificationsEnabled,
            ironLimitMinutes = ironLimitMinutes,
            isLoggingOut = isLoggingOut,
            isOnline = sync.isOnline,
            isSyncing = sync.isSyncing,
            lastSyncText = sync.lastSyncText
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
            runCatching { deviceRepository.applyIronLimitToAll(minutes) }
        }
    }

    fun setUserName(name: String) {
        viewModelScope.launch {
            settingsRepository.setUserName(name)
        }
    }

    fun syncNow() {
        if (syncFlow.value.isSyncing) return
        db.goOnline()
        viewModelScope.launch {
            syncFlow.value = syncFlow.value.copy(isSyncing = true, lastSyncText = "—")
            delay(1500)
            syncFlow.value = syncFlow.value.copy(isSyncing = false, lastSyncText = "Just now")
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
