package com.rafario.lahrecetah.domain.usecase.users

import com.rafario.lahrecetah.data.repository.AuthRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val clearSessionUseCase: ClearSessionUseCase
) {
    suspend operator fun invoke() {
        authRepository.logout()      // Firebase
        clearSessionUseCase()        // DataStore (rememberMe = false)
    }
}