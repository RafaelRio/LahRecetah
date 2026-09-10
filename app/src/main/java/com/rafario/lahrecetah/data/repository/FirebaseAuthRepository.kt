package com.rafario.lahrecetah.data.repository

import com.google.firebase.auth.GoogleAuthProvider
import com.rafario.lahrecetah.data.remote.auth.FirebaseAuthDataSource
import com.rafario.lahrecetah.domain.model.AuthUser
import com.rafario.lahrecetah.domain.repository.AuthRepository
import javax.inject.Inject

class FirebaseAuthRepository @Inject constructor(
    private val dataSource: FirebaseAuthDataSource
) : AuthRepository {
    override suspend fun login(email: String, password: String): Result<AuthUser> {
        return dataSource.login(email, password)
            .map { firebaseUser ->
                AuthUser(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email.orEmpty(),
                    displayName = firebaseUser.displayName
                )
            }
    }

    override suspend fun loginWithGoogle(idToken: String): Result<AuthUser> {
        return dataSource.loginWithGoogle(GoogleAuthProvider.getCredential(idToken, null))
            .map { firebaseUser ->
                AuthUser(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email.orEmpty(),
                    displayName = firebaseUser.displayName
                )
            }
    }

    override suspend fun register(
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

    override fun logout() = dataSource.logout()

    override fun getCurrentUser(): AuthUser? =
        dataSource.getCurrentUser()
            ?.takeIf { it.isEmailVerified }
            ?.let {
                AuthUser(
                    uid = it.uid,
                    email = it.email.orEmpty(),
                    displayName = it.displayName.orEmpty()
                )
            }

    override suspend fun updateDisplayName(newName: String) = dataSource.updateDisplayName(newName)

}