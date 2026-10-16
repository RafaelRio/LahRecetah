package com.rafario.lahrecetah.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafario.lahrecetah.domain.auth.AuthException
import com.rafario.lahrecetah.domain.repository.AuthRepository
import com.rafario.lahrecetah.domain.repository.SessionRepository
import com.rafario.lahrecetah.domain.usecase.users.GoogleLoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val loginWithGoogleUseCase: GoogleLoginUseCase,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    private val _loginEvent = MutableSharedFlow<LoginEvent>()
    val loginEvent = _loginEvent.asSharedFlow()

    fun onEmailChanged(value: String) {
        _uiState.update {
            it.copy(email = value)
        }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update {
            it.copy(password = value)
        }
    }

    fun onRememberMeChanged(value: Boolean) {
        _uiState.update {
            it.copy(rememberMe = value)
        }
    }

    fun login() {
        val state = _uiState.value

        if (state.isLoading) {
            return
        }

        if (state.email.isBlank() || state.password.isBlank()) {
            emitError(LoginUiError.MissingCredentials)
            return
        }

        _uiState.update {
            it.copy(isLoading = true)
        }

        viewModelScope.launch {
            try {
                val result = authRepository.login(
                    state.email.trim(),
                    state.password
                )

                val failure = result.exceptionOrNull()
                if (failure is CancellationException) throw failure

                if (result.isSuccess) {
                    try {
                        sessionRepository.saveRememberMe(state.rememberMe)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (_: Exception) {
                        _loginEvent.emit(LoginEvent.Error(LoginUiError.SessionPersistence))
                        return@launch
                    }

                    _loginEvent.emit(LoginEvent.Success)
                } else {
                    val error = when (failure) {
                        is AuthException -> LoginUiError.Auth(failure.error)
                        else -> LoginUiError.Unexpected
                    }

                    _loginEvent.emit(LoginEvent.Error(error))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _loginEvent.emit(LoginEvent.Error(LoginUiError.Unexpected))
            } finally {
                _uiState.update {
                    it.copy(isLoading = false)
                }
            }
        }
    }

    fun loginWithGoogle(idToken: String) {
        if (_uiState.value.isLoading) {
            return
        }

        _uiState.update {
            it.copy(isLoading = true)
        }

        viewModelScope.launch {
            try {
                val result = loginWithGoogleUseCase(idToken)

                val failure = result.exceptionOrNull()
                if (failure is CancellationException) throw failure

                if (result.isSuccess) {
                    try {
                        sessionRepository.saveRememberMe(true)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (_: Exception) {
                        _loginEvent.emit(LoginEvent.Error(LoginUiError.SessionPersistence))
                        return@launch
                    }

                    _loginEvent.emit(LoginEvent.Success)
                } else {
                    val error = when (failure) {
                        is AuthException -> LoginUiError.Auth(failure.error)
                        else -> LoginUiError.Unexpected
                    }
                    _loginEvent.emit(LoginEvent.Error(error))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _loginEvent.emit(LoginEvent.Error(LoginUiError.Unexpected))
            } finally {
                _uiState.update {
                    it.copy(isLoading = false)
                }
            }
        }
    }

    private fun emitError(error: LoginUiError) {
        viewModelScope.launch {
            _loginEvent.emit(
                LoginEvent.Error(error)
            )
        }
    }
}

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val rememberMe: Boolean = false,
    val isLoading: Boolean = false
)

sealed class LoginEvent {
    data object Success : LoginEvent()

    data class Error(
        val error: LoginUiError
    ) : LoginEvent()
}