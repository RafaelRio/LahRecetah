package com.rafario.lahrecetah.data.repository

import com.rafario.lahrecetah.data.remote.firestore.UserFirestoreDataSource
import com.rafario.lahrecetah.domain.model.UserProfile
import javax.inject.Inject

class FirestoreRepository @Inject constructor(
    private val dataSource: UserFirestoreDataSource
) {

    suspend fun createUserProfile(profile: UserProfile) {
        dataSource.createUserProfile(profile)
    }
}