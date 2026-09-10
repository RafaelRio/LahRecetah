package com.rafario.lahrecetah.domain.usecase.users

import com.rafario.lahrecetah.domain.repository.SessionRepository
import javax.inject.Inject

class SaveRememberMeUseCase @Inject constructor(
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(value: Boolean) {
        sessionRepository.saveRememberMe(value)
    }
}