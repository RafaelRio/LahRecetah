package com.rafario.lahrecetah.domain.usecase.users

import com.rafario.lahrecetah.domain.repository.AuthRepository
import com.rafario.lahrecetah.domain.repository.SessionRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke() {
        authRepository.logout()
        sessionRepository.clear()
    }
}