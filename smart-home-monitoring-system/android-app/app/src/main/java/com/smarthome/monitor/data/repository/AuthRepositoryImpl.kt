package com.smarthome.monitor.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.smarthome.monitor.data.model.User
import com.smarthome.monitor.domain.repository.AuthRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth
) : AuthRepository {
    override val currentUserId: String?
        get() = auth.currentUser?.uid

    override suspend fun ensureSignedIn(): User {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            return User(uid = currentUser.uid, homeId = "home_001")
        }
        val result = auth.signInAnonymously().await()
        val user = result.user ?: throw IllegalStateException("Firebase Auth failed to return a user")
        return User(uid = user.uid, homeId = "home_001")
    }

    override suspend fun signOut() {
        auth.signOut()
    }
}
