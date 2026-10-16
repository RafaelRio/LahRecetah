package com.rafario.lahrecetah.ui.login

import com.rafario.lahrecetah.domain.auth.AuthError

sealed interface LoginUiError {
    data object MissingCredentials : LoginUiError

    data class Auth(
        val reason: AuthError
    ) : LoginUiError

    data object SessionPersistence : LoginUiError

    data object Unexpected : LoginUiError
}