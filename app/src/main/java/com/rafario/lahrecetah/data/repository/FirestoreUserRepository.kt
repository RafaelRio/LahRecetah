package com.rafario.lahrecetah.data.repository

import com.rafario.lahrecetah.data.remote.firestore.UserFirestoreDataSource
import com.rafario.lahrecetah.domain.model.UserProfile
import com.rafario.lahrecetah.domain.repository.UserRepository
import javax.inject.Inject

class FirestoreUserRepository @Inject constructor(
    private val dataSource: UserFirestoreDataSource
) : UserRepository {

    override suspend fun createUserProfile(profile: UserProfile) {
        dataSource.createUserProfile(profile)
    }

    override suspend fun userExists(email: String): Boolean {
        return dataSource.userExists(email)
    }

    override suspend fun getUserProfile(email: String): UserProfile? =
        dataSource.getUserProfile(email)

    override suspend fun updateUserName(email: String, newName: String) =
        dataSource.updateUserName(email, newName)
}