package com.rafario.lahrecetah.data.repository

import com.rafario.lahrecetah.data.remote.auth.FirebaseAuthDataSource
import com.rafario.lahrecetah.domain.model.User
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val dataSource: FirebaseAuthDataSource
) {
    suspend fun login(email: String, password: String): Result<User> {
        return dataSource.login(email, password)
            .map { firebaseUser ->
                User(email = firebaseUser.email ?: "")
            }
    }

    suspend fun register(
        name: String,
        email: String,
        password: String
    ): Result<Unit> {
        return dataSource.register(name, email, password)
    }

    fun logout() = dataSource.logout()

    fun getCurrentUser(): User? =
        dataSource.getCurrentUser()
            ?.takeIf { it.isEmailVerified }
            ?.let { User(it.email ?: "") }
}