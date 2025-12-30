package com.rafario.lahrecetah.domain.usecase

import com.rafario.lahrecetah.data.repository.AuthRepository
import com.rafario.lahrecetah.domain.model.User
import javax.inject.Inject

class LoginUserUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<User> {
        return repository.login(email, password)
    }
}