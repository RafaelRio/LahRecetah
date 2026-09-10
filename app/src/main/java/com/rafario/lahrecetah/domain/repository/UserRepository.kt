package com.rafario.lahrecetah.domain.repository

import com.rafario.lahrecetah.domain.model.UserProfile

interface UserRepository {
    suspend fun createUserProfile(profile: UserProfile)
    suspend fun userExists(email: String): Boolean
    suspend fun getUserProfile(email: String): UserProfile?
    suspend fun updateUserName(email: String, newName: String)
}
