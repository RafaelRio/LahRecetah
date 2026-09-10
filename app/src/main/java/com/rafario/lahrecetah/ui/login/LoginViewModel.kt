package com.rafario.lahrecetah.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafario.lahrecetah.domain.usecase.users.GoogleLoginUseCase
import com.rafario.lahrecetah.domain.usecase.users.LoginUserUseCase
import com.rafario.lahrecetah.domain.usecase.users.SaveRememberMeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUserUseCase: LoginUserUseCase,
    private val loginWithGoogleUseCase: GoogleLoginUseCase,
    private val saveRememberMeUseCase: SaveRememberMeUseCase
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
            emitError("Email y contraseña obligatorios")
            return
        }

        _uiState.update {
            it.copy(isLoading = true)
        }

        viewModelScope.launch {
            try {
                val result = loginUserUseCase(
                    state.email.trim(),
                    state.password
                )

                val failure = result.exceptionOrNull()
                if (failure is CancellationException) throw failure

                if (result.isSuccess) {
                    saveRememberMeUseCase(state.rememberMe)

                    _loginEvent.emit(LoginEvent.Success)
                } else {
                    _loginEvent.emit(
                        LoginEvent.Error(
                            failure?.message
                                ?: "No se pudo iniciar sesión"
                        )
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _loginEvent.emit(
                    LoginEvent.Error(
                        e.message ?: "Error inesperado al iniciar sesión"
                    )
                )
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
                    saveRememberMeUseCase(true)

                    _loginEvent.emit(LoginEvent.Success)
                } else {
                    _loginEvent.emit(
                        LoginEvent.Error(
                            failure?.message
                                ?: "No se pudo iniciar sesión con Google"
                        )
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _loginEvent.emit(
                    LoginEvent.Error(
                        e.message ?: "Error inesperado al iniciar sesión con Google"
                    )
                )
            } finally {
                _uiState.update {
                    it.copy(isLoading = false)
                }
            }
        }
    }

    private fun emitError(message: String) {
        viewModelScope.launch {
            _loginEvent.emit(
                LoginEvent.Error(message)
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
        val message: String?
    ) : LoginEvent()
}