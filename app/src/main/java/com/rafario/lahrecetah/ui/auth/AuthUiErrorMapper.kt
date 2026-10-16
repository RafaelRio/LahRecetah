package com.rafario.lahrecetah.ui.auth

import androidx.annotation.StringRes
import com.rafario.lahrecetah.R
import com.rafario.lahrecetah.domain.auth.AuthError
import com.rafario.lahrecetah.ui.login.LoginUiError
import com.rafario.lahrecetah.ui.register.RegisterUiError

@StringRes
fun AuthError.toStringRes(): Int = when (this) {
    AuthError.INVALID_CREDENTIALS -> R.string.auth_invalid_credentials
    AuthError.EMAIL_NOT_VERIFIED -> R.string.auth_email_not_verified
    AuthError.NETWORK -> R.string.auth_network_error
    AuthError.USER_DISABLED -> R.string.auth_user_disabled
    AuthError.EMAIL_ALREADY_IN_USE -> R.string.auth_email_already_in_use
    AuthError.WEAK_PASSWORD -> R.string.auth_weak_password
    AuthError.UNKNOWN -> R.string.auth_unknown_error
}

@StringRes
fun LoginUiError.toStringRes(): Int = when (this) {
    LoginUiError.MissingCredentials ->
        R.string.login_missing_credentials

    is LoginUiError.Auth ->
        reason.toStringRes()

    LoginUiError.SessionPersistence ->
        R.string.login_session_persistence_error

    LoginUiError.Unexpected ->
        R.string.unexpected_error
}

@StringRes
fun RegisterUiError.toStringRes(): Int = when (this) {
    RegisterUiError.MissingFields ->
        R.string.register_missing_fields

    is RegisterUiError.Auth ->
        reason.toStringRes()

    RegisterUiError.Unexpected ->
        R.string.unexpected_error
}