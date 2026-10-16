package com.rafario.lahrecetah.ui.auth

import com.rafario.lahrecetah.R
import com.rafario.lahrecetah.domain.auth.AuthError
import com.rafario.lahrecetah.ui.login.LoginUiError
import com.rafario.lahrecetah.ui.register.RegisterUiError
import org.junit.Assert.assertEquals
import org.junit.Test

class AuthUiErrorMapperTest {

    @Test
    fun `authentication errors use the expected messages in both screens`() {
        val expectedResources = mapOf(
            AuthError.INVALID_CREDENTIALS to R.string.auth_invalid_credentials,
            AuthError.EMAIL_NOT_VERIFIED to R.string.auth_email_not_verified,
            AuthError.NETWORK to R.string.auth_network_error,
            AuthError.USER_DISABLED to R.string.auth_user_disabled,
            AuthError.EMAIL_ALREADY_IN_USE to R.string.auth_email_already_in_use,
            AuthError.WEAK_PASSWORD to R.string.auth_weak_password,
            AuthError.UNKNOWN to R.string.auth_unknown_error
        )

        assertEquals(AuthError.entries.toSet(), expectedResources.keys)

        expectedResources.forEach { (reason, expectedResource) ->
            assertEquals(
                "Mensaje de autenticación incorrecto para $reason",
                expectedResource,
                reason.toStringRes()
            )
            assertEquals(
                "Mensaje de login incorrecto para $reason",
                expectedResource,
                LoginUiError.Auth(reason).toStringRes()
            )
            assertEquals(
                "Mensaje de registro incorrecto para $reason",
                expectedResource,
                RegisterUiError.Auth(reason).toStringRes()
            )
        }
    }

    @Test
    fun `login distinguishes missing credentials session failure and unexpected error`() {
        assertEquals(
            R.string.login_missing_credentials,
            LoginUiError.MissingCredentials.toStringRes()
        )
        assertEquals(
            R.string.login_session_persistence_error,
            LoginUiError.SessionPersistence.toStringRes()
        )
        assertEquals(
            R.string.unexpected_error,
            LoginUiError.Unexpected.toStringRes()
        )
    }

    @Test
    fun `registration distinguishes missing fields and unexpected error`() {
        assertEquals(
            R.string.register_missing_fields,
            RegisterUiError.MissingFields.toStringRes()
        )
        assertEquals(
            R.string.unexpected_error,
            RegisterUiError.Unexpected.toStringRes()
        )
    }
}