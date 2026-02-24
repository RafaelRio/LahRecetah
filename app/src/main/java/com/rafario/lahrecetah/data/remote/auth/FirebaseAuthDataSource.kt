package com.rafario.lahrecetah.data.remote.auth

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.coroutines.resume

class FirebaseAuthDataSource @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {

    suspend fun login(
        email: String,
        password: String
    ): Result<FirebaseUser> =
        suspendCancellableCoroutine { cont ->
            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val user = result.user

                    if (user != null && user.isEmailVerified) {
                        cont.resume(Result.success(user))
                    } else {
                        firebaseAuth.signOut()
                        cont.resume(Result.failure(Exception("Email no verificado")))
                    }
                }
                .addOnFailureListener {
                    cont.resume(Result.failure(it))
                }
        }

    suspend fun loginWithGoogle(credential: AuthCredential): Result<FirebaseUser> =
        suspendCancellableCoroutine { cont ->
            firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener { result ->
                    val user = result.user
                    if (user != null) {
                        cont.resume(Result.success(user))
                    } else {
                        cont.resume(Result.failure(Exception("Usuario nulo")))
                    }
                }
                .addOnFailureListener {
                    cont.resume(Result.failure(it))
                }
        }

    suspend fun register(
        name: String,
        email: String,
        password: String
    ): Result<String> =
        suspendCancellableCoroutine { cont ->
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val user = result.user ?: run {
                        cont.resume(Result.failure(Exception("Usuario nulo")))
                        return@addOnSuccessListener
                    }

                    val uid = user.uid

                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()

                    user.updateProfile(profileUpdates)
                        .continueWithTask {
                            user.sendEmailVerification()
                        }
                        .addOnSuccessListener {
                            firebaseAuth.signOut()
                            cont.resume(Result.success(uid)) // 👈 clave
                        }
                        .addOnFailureListener {
                            cont.resume(Result.failure(it))
                        }
                }
                .addOnFailureListener {
                    cont.resume(Result.failure(it))
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