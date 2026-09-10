package com.rafario.lahrecetah.data.remote.auth

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

class FirebaseAuthDataSource @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {
    suspend fun login(email: String, password: String): Result<FirebaseUser> {
        return try {
            val user = firebaseAuth.signInWithEmailAndPassword(email, password).await().user
            if (user != null && user.isEmailVerified) {
                Result.success(user)
            } else {
                firebaseAuth.signOut()
                Result.failure(Exception("Email no verificado"))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginWithGoogle(credential: AuthCredential): Result<FirebaseUser> {
        return try {
            val user = firebaseAuth.signInWithCredential(credential).await().user
                ?: return Result.failure(Exception("Usuario nulo"))
            Result.success(user)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(name: String, email: String, password: String): Result<String> {
        return try {
            val user = firebaseAuth.createUserWithEmailAndPassword(email, password).await().user
                ?: return Result.failure(Exception("Usuario nulo"))
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()
            user.updateProfile(profileUpdates).await()
            user.sendEmailVerification().await()
            Result.success(user.uid)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        firebaseAuth.signOut()
    }

    fun getCurrentUser(): FirebaseUser? = firebaseAuth.currentUser

    suspend fun updateDisplayName(newName: String) {
        val user = firebaseAuth.currentUser ?: return
        val request = UserProfileChangeRequest.Builder()
            .setDisplayName(newName)
            .build()
        user.updateProfile(request).await()
    }
}
