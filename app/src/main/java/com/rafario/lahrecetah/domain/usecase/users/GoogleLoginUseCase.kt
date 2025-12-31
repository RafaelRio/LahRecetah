package com.rafario.lahrecetah.domain.usecase.users

import com.google.firebase.auth.AuthCredential
import com.rafario.lahrecetah.data.repository.AuthRepository
import com.rafario.lahrecetah.data.repository.UserFirestoreRepository
import com.rafario.lahrecetah.domain.model.AuthUser
import com.rafario.lahrecetah.domain.model.UserProfile
import javax.inject.Inject

class GoogleLoginUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val firestoreRepository: UserFirestoreRepository
) {
    suspend operator fun invoke(credential: AuthCredential): Result<AuthUser> {
        return try {
            val authUser = authRepository.loginWithGoogle(credential).getOrThrow()

            // Verificar si el usuario ya existe en Firestore
            val userExists = firestoreRepository.userExists(authUser.uid)

            // Si es la primera vez que inicia sesión con Google, crear su perfil
            if (!userExists) {
                val profile = UserProfile(
                    uid = authUser.uid,
                    name = authUser.displayName ?: "Usuario Google",
                    email = authUser.email
                )
                firestoreRepository.createUserProfile(profile)
            }

            Result.success(authUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}