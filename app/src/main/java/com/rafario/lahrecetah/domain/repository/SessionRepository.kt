package com.rafario.lahrecetah.domain.repository

import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    val rememberMeFlow: Flow<Boolean>
    suspend fun saveRememberMe(value: Boolean)
    suspend fun clear()
}
