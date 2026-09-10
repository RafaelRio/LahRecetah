package com.rafario.lahrecetah.domain.usecase.users

import com.rafario.lahrecetah.domain.model.AuthUser
import com.rafario.lahrecetah.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUserUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<AuthUser> {
        return repository.login(email, password)
    }
}