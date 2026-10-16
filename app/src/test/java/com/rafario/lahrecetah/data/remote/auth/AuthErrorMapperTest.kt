package com.rafario.lahrecetah.data.remote.auth

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.rafario.lahrecetah.domain.auth.AuthError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.fail
import org.junit.Test
import kotlin.coroutines.cancellation.CancellationException
import android.app.Application
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(
    sdk = [33],
    manifest = Config.NONE,
    application = Application::class
)
class AuthErrorMapperTest {

    @Test
    fun `unverified email maps to typed error and preserves cause`() {
        val original = EmailNotVerifiedException()

        val result = original.toAuthException()

        assertEquals(AuthError.EMAIL_NOT_VERIFIED, result.error)
        assertSame(original, result.cause)
    }

    @Test
    fun `network failure maps to network error and preserves cause`() {
        val original = FirebaseNetworkException("Network unavailable")

        val result = original.toAuthException()

        assertEquals(AuthError.NETWORK, result.error)
        assertSame(original, result.cause)
    }

    @Test
    fun `invalid credentials map to typed error and preserve cause`() {
        val original = FirebaseAuthInvalidCredentialsException(
            "ERROR_INVALID_CREDENTIAL",
            "Invalid credentials"
        )

        val result = original.toAuthException()

        assertEquals(AuthError.INVALID_CREDENTIALS, result.error)
        assertSame(original, result.cause)
    }

    @Test
    fun `disabled user is identified by error code and preserves cause`() {
        val original = FirebaseAuthInvalidUserException(
            "ERROR_USER_DISABLED",
            "Un mensaje cualquiera"
        )

        val result = original.toAuthException()

        assertEquals(AuthError.USER_DISABLED, result.error)
        assertSame(original, result.cause)
    }

    @Test
    fun `other invalid user errors map to invalid credentials regardless of message`() {
        val original = FirebaseAuthInvalidUserException(
            "ERROR_USER_NOT_FOUND",
            "User disabled"
        )

        val result = original.toAuthException()

        assertEquals(AuthError.INVALID_CREDENTIALS, result.error)
        assertSame(original, result.cause)
    }

    @Test
    fun `email collision maps to email already in use and preserves cause`() {
        val original = FirebaseAuthUserCollisionException(
            "ERROR_EMAIL_ALREADY_IN_USE",
            "Email already in use"
        )

        val result = original.toAuthException()

        assertEquals(AuthError.EMAIL_ALREADY_IN_USE, result.error)
        assertSame(original, result.cause)
    }

    @Test
    fun `weak password maps to weak password instead of invalid credentials`() {
        val original = FirebaseAuthWeakPasswordException(
            "ERROR_WEAK_PASSWORD",
            "Weak password",
            "Password is too short"
        )

        val result = original.toAuthException()

        assertEquals(AuthError.WEAK_PASSWORD, result.error)
        assertSame(original, result.cause)
    }

    @Test
    fun `unknown exception is not classified by its message`() {
        val original = IllegalStateException("Email no verificado")

        val result = original.toAuthException()

        assertEquals(AuthError.UNKNOWN, result.error)
        assertSame(original, result.cause)
    }

    @Test
    fun `cancellation propagates without being wrapped`() {
        val original = CancellationException("Authentication cancelled")

        try {
            original.toAuthException()
            fail("Expected CancellationException")
        } catch (caught: CancellationException) {
            assertSame(original, caught)
        }
    }
}