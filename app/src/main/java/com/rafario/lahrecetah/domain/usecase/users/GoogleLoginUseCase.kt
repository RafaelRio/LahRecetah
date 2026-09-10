package com.rafario.lahrecetah.domain.usecase.users

import com.rafario.lahrecetah.domain.model.AuthUser
import com.rafario.lahrecetah.domain.model.UserProfile
import com.rafario.lahrecetah.domain.repository.AuthRepository
import com.rafario.lahrecetah.domain.repository.UserRepository
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class GoogleLoginUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(idToken: String): Result<AuthUser> {
        return try {
            val authUser = authRepository.loginWithGoogle(idToken).getOrThrow()

            // Verificar si el usuario ya existe en Firestore
            val userExists = userRepository.userExists(authUser.email)

            // Si es la primera vez que inicia sesión con Google, crear su perfil
            if (!userExists) {
                val profile = UserProfile(
                    uid = authUser.uid,
                    name = authUser.displayName ?: "Usuario Google",
                    email = authUser.email
                )
                userRepository.createUserProfile(profile)
            }

            Result.success(authUser)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}