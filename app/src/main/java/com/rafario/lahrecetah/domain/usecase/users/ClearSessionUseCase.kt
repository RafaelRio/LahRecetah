package com.rafario.lahrecetah.domain.usecase.users

import com.rafario.lahrecetah.data.local.session.SessionDataStore
import javax.inject.Inject

class ClearSessionUseCase @Inject constructor(
    private val sessionDataStore: SessionDataStore
) {
    suspend operator fun invoke() {
        sessionDataStore.clear()
    }
}