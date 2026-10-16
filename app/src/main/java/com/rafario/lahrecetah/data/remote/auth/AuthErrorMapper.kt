package com.rafario.lahrecetah.data.remote.auth

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.rafario.lahrecetah.domain.auth.AuthError
import com.rafario.lahrecetah.domain.auth.AuthException
import kotlin.coroutines.cancellation.CancellationException

internal fun Throwable.toAuthException(): AuthException {
    if (this is CancellationException) throw this

    val error = when (this) {
        is EmailNotVerifiedException -> AuthError.EMAIL_NOT_VERIFIED
        is FirebaseNetworkException -> AuthError.NETWORK
        is FirebaseAuthWeakPasswordException -> AuthError.WEAK_PASSWORD
        is FirebaseAuthInvalidCredentialsException -> AuthError.INVALID_CREDENTIALS

        is FirebaseAuthInvalidUserException -> {
            if (errorCode == "ERROR_USER_DISABLED") {
                AuthError.USER_DISABLED
            } else {
                AuthError.INVALID_CREDENTIALS
            }
        }

        is FirebaseAuthUserCollisionException -> AuthError.EMAIL_ALREADY_IN_USE
        else -> AuthError.UNKNOWN
    }

    return AuthException(error, this)
}