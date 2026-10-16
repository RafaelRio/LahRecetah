package com.rafario.lahrecetah.data.repository

import com.google.firebase.auth.GoogleAuthProvider
import com.rafario.lahrecetah.data.remote.auth.FirebaseAuthDataSource
import com.rafario.lahrecetah.data.remote.auth.toAuthException
import com.rafario.lahrecetah.domain.model.AuthUser
import com.rafario.lahrecetah.domain.repository.AuthRepository
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class FirebaseAuthRepository @Inject constructor(
    private val dataSource: FirebaseAuthDataSource
) : AuthRepository {
    override suspend fun login(email: String, password: String): Result<AuthUser> {
        return try {
            val firebaseUser = dataSource.login(email, password).getOrThrow()

            Result.success(
                AuthUser(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email.orEmpty(),
                    displayName = firebaseUser.displayName
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e.toAuthException())
        }
    }

    override suspend fun loginWithGoogle(idToken: String): Result<AuthUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val firebaseUser = dataSource.loginWithGoogle(credential).getOrThrow()

            Result.success(
                AuthUser(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email.orEmpty(),
                    displayName = firebaseUser.displayName
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e.toAuthException())
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