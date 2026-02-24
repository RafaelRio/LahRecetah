package com.rafario.lahrecetah.data.repository

import com.google.firebase.auth.AuthCredential
import com.rafario.lahrecetah.data.remote.auth.FirebaseAuthDataSource
import com.rafario.lahrecetah.domain.model.AuthUser
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val dataSource: FirebaseAuthDataSource
) {
    suspend fun login(email: String, password: String): Result<AuthUser> {
        return dataSource.login(email, password)
            .map { firebaseUser ->
                AuthUser(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email.orEmpty(),
                    displayName = firebaseUser.displayName
                )
            }
    }

    suspend fun loginWithGoogle(credential: AuthCredential): Result<AuthUser> {
        return dataSource.loginWithGoogle(credential)
            .map { firebaseUser ->
                AuthUser(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email.orEmpty(),
                    displayName = firebaseUser.displayName
                )
            }
    }

    suspend fun register(
        name: String,
        email: String,
        password: String
    ): Result<AuthUser> {
        return dataSource.register(name, email, password)
            .map { uid ->
                AuthUser(
                    uid = uid,
                    email = email
                )
            }
    }

    fun logout() = dataSource.logout()

    fun getCurrentUser(): AuthUser? =
        dataSource.getCurrentUser()
            ?.takeIf { it.isEmailVerified }
            ?.let {
                AuthUser(
                    uid = it.uid,
                    email = it.email.orEmpty(),
                    displayName = it.displayName.orEmpty()
                )
            }

    suspend fun updateDisplayName(newName: String) = dataSource.updateDisplayName(newName)

}