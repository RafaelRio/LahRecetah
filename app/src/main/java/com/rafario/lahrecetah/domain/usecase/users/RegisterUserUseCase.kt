package com.rafario.lahrecetah.domain.usecase.users

import com.rafario.lahrecetah.data.repository.AuthRepository
import com.rafario.lahrecetah.data.repository.UserFirestoreRepository
import com.rafario.lahrecetah.domain.model.UserProfile
import javax.inject.Inject

class RegisterUserUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserFirestoreRepository
) {

    suspend operator fun invoke(
        name: String,
        email: String,
        password: String
    ): Result<Unit> {
        return try {
            val authUser = authRepository
                .register(name, email, password)
                .getOrThrow()

            val profile = UserProfile(
                uid = authUser.uid,
                name = name,
                email = authUser.email
            )

            userRepository.createUserProfile(profile)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}