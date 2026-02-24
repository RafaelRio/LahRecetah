package com.rafario.lahrecetah.data.repository

import com.rafario.lahrecetah.data.remote.firestore.UserFirestoreDataSource
import com.rafario.lahrecetah.domain.model.UserProfile
import javax.inject.Inject

class UserFirestoreRepository @Inject constructor(
    private val dataSource: UserFirestoreDataSource
) {

    suspend fun createUserProfile(profile: UserProfile) {
        dataSource.createUserProfile(profile)
    }

    suspend fun userExists(email: String): Boolean {
        return dataSource.userExists(email)
    }

    suspend fun getUserProfile(email: String): UserProfile? =
        dataSource.getUserProfile(email)

    suspend fun updateUserName(email: String, newName: String) =
        dataSource.updateUserName(email, newName)
}