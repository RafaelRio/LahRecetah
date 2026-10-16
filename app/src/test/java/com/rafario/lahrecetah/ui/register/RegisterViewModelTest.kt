package com.rafario.lahrecetah.ui.register
import com.rafario.lahrecetah.domain.auth.AuthError
import com.rafario.lahrecetah.domain.auth.AuthException
import com.rafario.lahrecetah.domain.usecase.users.RegisterUserUseCase
import com.rafario.lahrecetah.testing.FakeAuthRepository
import com.rafario.lahrecetah.testing.FakeUserRepository
import com.rafario.lahrecetah.testing.MainDispatcherRule
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RegisterViewModelTest {
    @get:Rule
    val main = MainDispatcherRule()

    private val auth = FakeAuthRepository()
    private val users = FakeUserRepository()

    private fun viewModel() = RegisterViewModel(
        RegisterUserUseCase(auth, users)
    ).apply {
        onNameChanged(" Chef ")
        onEmailChanged(" cook@example.com ")
        onPasswordChanged("password")
    }

    private fun TestScope.events(vm: RegisterViewModel): MutableList<RegisterEvent> {
        val events = mutableListOf<RegisterEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.registerEvent.collect { events += it }
        }
        return events
    }

    @Test
    fun `empty fields emit typed error without registering`() = runTest {
        for (emptyField in listOf("name", "email", "password")) {
            val vm = viewModel()
            val events = events(vm)
            when (emptyField) {
                "name" -> vm.onNameChanged(" ")
                "email" -> vm.onEmailChanged(" ")
                else -> vm.onPasswordChanged(" ")
            }

            vm.register()
            runCurrent()

            assertEquals(
                listOf(RegisterEvent.Error(RegisterUiError.MissingFields)),
                events
            )
            assertFalse(vm.uiState.value.isLoading)
        }

        assertEquals(0, auth.registerCalls)
    }

    @Test
    fun `successful registration emits success clears fields and logs out`() = runTest {
        val vm = viewModel()
        val events = events(vm)

        vm.register()
        runCurrent()

        assertEquals(listOf(RegisterEvent.Success), events)
        assertEquals("Chef", users.profiles.single().name)
        assertEquals("cook@example.com", users.profiles.single().email)
        assertEquals("", vm.uiState.value.name)
        assertEquals("", vm.uiState.value.email)
        assertEquals("", vm.uiState.value.password)
        assertEquals(1, auth.logoutCalls)
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun `auth failure emits typed error and logs out`() = runTest {
        auth.registerResult = Result.failure(
            AuthException(AuthError.EMAIL_ALREADY_IN_USE, IllegalStateException())
        )
        val vm = viewModel()
        val events = events(vm)

        vm.register()
        runCurrent()

        assertEquals(
            listOf(RegisterEvent.Error(
                RegisterUiError.Auth(AuthError.EMAIL_ALREADY_IN_USE)
            )),
            events
        )
        assertEquals(1, auth.logoutCalls)
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun `profile failure emits unexpected error and logs out`() = runTest {
        users.createProfileFailure = IllegalStateException("profile failed")
        val vm = viewModel()
        val events = events(vm)

        vm.register()
        runCurrent()

        assertEquals(
            listOf(RegisterEvent.Error(RegisterUiError.Unexpected)),
            events
        )
        assertEquals(1, auth.logoutCalls)
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun `cancellation emits no error and logs out`() = runTest {
        auth.registerResult = Result.failure(CancellationException("cancel"))
        val vm = viewModel()
        val events = events(vm)

        vm.register()
        runCurrent()

        assertTrue(events.isEmpty())
        assertEquals(1, auth.logoutCalls)
        assertFalse(vm.uiState.value.isLoading)
    }
}