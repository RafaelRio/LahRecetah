package com.rafario.lahrecetah.domain.usecase.users


import com.rafario.lahrecetah.testing.FakeAuthRepository
import com.rafario.lahrecetah.testing.FakeSessionRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class LogoutUseCaseTest {
    @Test
    fun `logs out and clears session`() = runTest {
        val auth = FakeAuthRepository()
        val session = FakeSessionRepository().apply {
            rememberMeFlow.value = true
        }

        LogoutUseCase(auth, session).invoke()

        assertNull(auth.getCurrentUser())
        assertFalse(session.rememberMeFlow.value)
    }
}