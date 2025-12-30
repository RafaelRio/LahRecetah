package com.rafario.lahrecetah.domain.usecase

import com.rafario.lahrecetah.data.repository.AuthRepository
import javax.inject.Inject

class RegisterUserUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(
        name: String,
        email: String,
        password: String
    ): Result<Unit> {
        return repository.register(name, email, password)
    }
}