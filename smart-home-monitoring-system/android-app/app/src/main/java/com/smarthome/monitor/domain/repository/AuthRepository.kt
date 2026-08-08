package com.smarthome.monitor.domain.repository

import com.smarthome.monitor.data.model.User

interface AuthRepository {
    val currentUserId: String?
    suspend fun ensureSignedIn(): User
}
