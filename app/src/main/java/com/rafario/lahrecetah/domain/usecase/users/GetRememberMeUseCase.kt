package com.rafario.lahrecetah.domain.usecase.users

import com.rafario.lahrecetah.domain.repository.SessionRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetRememberMeUseCase @Inject constructor(
    private val sessionRepository: SessionRepository
) {
    operator fun invoke(): Flow<Boolean> =
        sessionRepository.rememberMeFlow
}