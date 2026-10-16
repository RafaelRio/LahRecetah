package com.rafario.lahrecetah.ui.register

import com.rafario.lahrecetah.domain.auth.AuthError

sealed interface RegisterUiError {
    data object MissingFields : RegisterUiError

    data class Auth(
        val reason: AuthError
    ) : RegisterUiError

    data object Unexpected : RegisterUiError
}