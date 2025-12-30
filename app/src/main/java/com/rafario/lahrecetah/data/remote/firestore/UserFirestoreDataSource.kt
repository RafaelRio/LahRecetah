package com.rafario.lahrecetah.data.remote.firestore

import com.google.firebase.firestore.FirebaseFirestore
import com.rafario.lahrecetah.domain.model.UserProfile
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserFirestoreDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    suspend fun createUserProfile(profile: UserProfile) {
        firestore.collection("users")
            .document(profile.email)
            .set(
                mapOf(
                    "name" to profile.name,
                    "email" to profile.email
                )
            )
            .await()
    }

    suspend fun userExists(uid: String): Boolean {
        return try {
            val document = firestore.collection("users")
                .document(uid)
                .get()
                .await()
            document.exists()
        } catch (e: Exception) {
            false
        }
    }
}