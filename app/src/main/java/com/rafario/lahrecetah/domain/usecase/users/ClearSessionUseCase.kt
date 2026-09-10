package com.rafario.lahrecetah.domain.usecase.users

import com.rafario.lahrecetah.domain.repository.SessionRepository
import javax.inject.Inject

class ClearSessionUseCase @Inject constructor(
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke() {
        sessionRepository.clear()
    }
}