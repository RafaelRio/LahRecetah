package com.rafario.lahrecetah.domain.auth

enum class AuthError {
    INVALID_CREDENTIALS,
    EMAIL_NOT_VERIFIED,
    NETWORK,
    USER_DISABLED,
    EMAIL_ALREADY_IN_USE,
    WEAK_PASSWORD,
    UNKNOWN
}