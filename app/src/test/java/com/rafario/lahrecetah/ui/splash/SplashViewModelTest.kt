package com.rafario.lahrecetah.ui.splash

import com.rafario.lahrecetah.testing.FakeAuthRepository
import com.rafario.lahrecetah.testing.FakeSessionRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SplashViewModelTest {
    @Test
    fun `main requires remember me and an authenticated user`() = runTest {
        val session = FakeSessionRepository()
        val auth = FakeAuthRepository()
        val viewModel = SplashViewModel(session, auth)

        assertEquals("login", viewModel.startDestination.first())

        session.rememberMeFlow.value = true
        assertEquals("main_screen", viewModel.startDestination.first())

        auth.user = null
        val unauthenticatedViewModel = SplashViewModel(session, auth)
        assertEquals("login", unauthenticatedViewModel.startDestination.first())
    }
}