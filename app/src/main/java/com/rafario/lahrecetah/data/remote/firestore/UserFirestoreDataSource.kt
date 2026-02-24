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
                    "uid" to profile.uid,
                    "name" to profile.name,
                    "email" to profile.email
                )
            )
            .await()
    }

    suspend fun userExists(email: String): Boolean {
        return try {
            val document = firestore.collection("users")
                .document(email)
                .get()
                .await()
            document.exists()
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getUserProfile(email: String): UserProfile? {
        val doc = firestore.collection("users").document(email).get().await()
        if (!doc.exists()) return null

        val uid = doc.getString("uid").orEmpty()
        val name = doc.getString("name").orEmpty()
        val mail = doc.getString("email") ?: email

        return UserProfile(
            uid = uid,
            name = name,
            email = mail
        )
    }

    suspend fun updateUserName(email: String, newName: String) {
        firestore.collection("users")
            .document(email)
            .update("name", newName)
            .await()
    }


}