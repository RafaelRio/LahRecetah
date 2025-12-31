package com.rafario.lahrecetah.domain.usecase.users

import com.rafario.lahrecetah.data.local.session.SessionDataStore
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRememberMeUseCase @Inject constructor(
    private val sessionDataStore: SessionDataStore
) {
    operator fun invoke(): Flow<Boolean> =
        sessionDataStore.rememberMeFlow
}