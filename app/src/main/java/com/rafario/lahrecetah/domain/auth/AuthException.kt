package com.rafario.lahrecetah.domain.auth

class AuthException(
    val error: AuthError,
    cause: Throwable
) : Exception(cause)