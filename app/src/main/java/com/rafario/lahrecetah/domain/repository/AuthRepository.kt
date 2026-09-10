package com.rafario.lahrecetah.domain.repository

import com.rafario.lahrecetah.domain.model.AuthUser

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<AuthUser>
    suspend fun loginWithGoogle(idToken: String): Result<AuthUser>
    suspend fun register(name: String, email: String, password: String): Result<AuthUser>
    fun logout()
    fun getCurrentUser(): AuthUser?
    suspend fun updateDisplayName(newName: String)
}
