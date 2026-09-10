package com.rafario.lahrecetah.ui.login

import com.rafario.lahrecetah.domain.usecase.users.GoogleLoginUseCase
import com.rafario.lahrecetah.domain.usecase.users.LoginUserUseCase
import com.rafario.lahrecetah.domain.usecase.users.SaveRememberMeUseCase
import com.rafario.lahrecetah.testing.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {
    @get:Rule val main = MainDispatcherRule()
    private val auth = FakeAuthRepository()
    private val session = FakeSessionRepository()
    private val users = FakeUserRepository()
    private fun viewModel() = LoginViewModel(
        LoginUserUseCase(auth), GoogleLoginUseCase(auth, users), SaveRememberMeUseCase(session)
    ).apply {
        onEmailChanged(" cook@example.com ")
        onPasswordChanged("password")
    }

    private fun TestScope.events(vm: LoginViewModel): MutableList<LoginEvent> {
        val events = mutableListOf<LoginEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.loginEvent.collect { events += it }
        }
        return events
    }

    @Test fun `empty fields emit error without logging in`() = runTest {
        for (emailEmpty in listOf(true, false)) {
            val vm = viewModel()
            val events = events(vm)
            if (emailEmpty) vm.onEmailChanged(" ") else vm.onPasswordChanged("")
            vm.login()
            runCurrent()
            assertTrue(events.single() is LoginEvent.Error)
            assertFalse(vm.uiState.value.isLoading)
        }
        assertEquals(0, auth.loginCalls)
        assertTrue(session.savedValues.isEmpty())
    }

    @Test fun `email success emits once and saves selected remember me once`() = runTest {
        val vm = viewModel()
        val events = events(vm)
        vm.onRememberMeChanged(true)
        vm.login()
        assertTrue(vm.uiState.value.isLoading)
        runCurrent()
        assertEquals(listOf(LoginEvent.Success), events)
        assertEquals(listOf(true), session.savedValues)
        assertEquals("cook@example.com", auth.receivedEmail)
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test fun `email login preserves unchecked remember me`() = runTest {
        val vm = viewModel()
        events(vm)
        vm.login()
        runCurrent()
        assertEquals(listOf(false), session.savedValues)
    }

    @Test fun `google success uses token saves once and creates profile`() = runTest {
        val vm = viewModel()
        val events = events(vm)
        vm.loginWithGoogle("google-token")
        runCurrent()
        assertEquals("google-token", auth.receivedToken)
        assertEquals(listOf(true), session.savedValues)
        assertEquals(listOf(LoginEvent.Success), events)
        assertEquals(auth.user?.uid, users.profiles.single().uid)
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test fun `both login failures emit error and restore loading`() = runTest {
        auth.loginResult = Result.failure(IllegalStateException("login failed"))
        for (google in listOf(false, true)) {
            val vm = viewModel()
            val events = events(vm)
            if (google) vm.loginWithGoogle("token") else vm.login()
            runCurrent()
            assertEquals(listOf(LoginEvent.Error("login failed")), events)
            assertFalse(vm.uiState.value.isLoading)
        }
        assertTrue(session.savedValues.isEmpty())
    }

    @Test fun `remember me failure emits error instead of success`() = runTest {
        session.saveFailure = IllegalStateException("storage failed")
        val vm = viewModel()
        val events = events(vm)
        vm.loginWithGoogle("token")
        runCurrent()
        assertEquals(listOf(LoginEvent.Error("storage failed")), events)
        assertEquals(listOf(true), session.savedValues)
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test fun `in flight login blocks both methods`() = runTest {
        for (google in listOf(false, true)) {
            val gate = CompletableDeferred<Unit>()
            auth.beforeLogin = { gate.await() }
            val vm = viewModel()
            val events = events(vm)
            val previousCalls = auth.loginCalls + auth.googleCalls
            if (google) vm.loginWithGoogle("token") else vm.login()
            runCurrent()
            vm.login()
            vm.loginWithGoogle("other")
            runCurrent()
            assertEquals(previousCalls + 1, auth.loginCalls + auth.googleCalls)
            assertTrue(vm.uiState.value.isLoading)
            gate.complete(Unit)
            runCurrent()
            assertEquals(listOf(LoginEvent.Success), events)
            assertFalse(vm.uiState.value.isLoading)
        }
    }

    @Test fun `cancellation produces no event and restores loading`() = runTest {
        auth.beforeLogin = { throw CancellationException("cancel") }
        for (google in listOf(false, true)) {
            val vm = viewModel()
            val events = events(vm)
            if (google) vm.loginWithGoogle("token") else vm.login()
            runCurrent()
            assertTrue(events.isEmpty())
            assertFalse(vm.uiState.value.isLoading)
        }
        assertTrue(session.savedValues.isEmpty())
    }
    @Test fun `cancellation in result is not shown as a login error`() = runTest {
        auth.loginResult = Result.failure(CancellationException("cancel"))
        for (google in listOf(false, true)) {
            val vm = viewModel()
            val events = events(vm)
            if (google) vm.loginWithGoogle("token") else vm.login()
            runCurrent()
            assertTrue(events.isEmpty())
            assertFalse(vm.uiState.value.isLoading)
        }
        assertTrue(session.savedValues.isEmpty())
    }

    @Test fun `cancellation while saving preferences restores loading without success`() = runTest {
        session.saveFailure = CancellationException("cancel")
        val vm = viewModel()
        val events = events(vm)
        vm.loginWithGoogle("token")
        runCurrent()
        assertTrue(events.isEmpty())
        assertEquals(listOf(true), session.savedValues)
        assertFalse(vm.uiState.value.isLoading)
    }

}
