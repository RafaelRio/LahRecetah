package com.rafario.lahrecetah.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafario.lahrecetah.domain.auth.AuthException
import com.rafario.lahrecetah.domain.usecase.users.RegisterUserUseCase
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
class RegisterViewModel @Inject constructor(
    private val registerUserUseCase: RegisterUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState = _uiState.asStateFlow()

    private val _registerEvent = MutableSharedFlow<RegisterEvent>()
    val registerEvent = _registerEvent.asSharedFlow()

    fun onNameChanged(value: String) {
        _uiState.update {
            it.copy(name = value)
        }
    }

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

    fun register() {
        val state = _uiState.value

        if (state.isLoading) {
            return
        }

        if (state.name.isBlank() || state.email.isBlank() || state.password.isBlank()) {
            emitError(RegisterUiError.MissingFields)
            return
        }

        _uiState.update {
            it.copy(isLoading = true)
        }

        viewModelScope.launch {
            try {
                val result = registerUserUseCase(
                    name = state.name.trim(), email = state.email.trim(), password = state.password
                )

                val failure = result.exceptionOrNull()
                if (failure is CancellationException) throw failure

                if (result.isSuccess) {
                    clearFields()
                    _registerEvent.emit(
                        RegisterEvent.Success
                    )
                } else {
                    val error = when (failure) {
                        is AuthException -> RegisterUiError.Auth(failure.error)
                        else -> RegisterUiError.Unexpected
                    }

                    _registerEvent.emit(RegisterEvent.Error(error))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _registerEvent.emit(RegisterEvent.Error(RegisterUiError.Unexpected))
            } finally {
                _uiState.update {
                    it.copy(isLoading = false)
                }
            }
        }
    }

    private fun clearFields() {
        _uiState.update {
            it.copy(
                name = "", email = "", password = ""
            )
        }
    }

    private fun emitError(error: RegisterUiError) {
        viewModelScope.launch {
            _registerEvent.emit(
                RegisterEvent.Error(error)
            )
        }
    }
}

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false
)

sealed class RegisterEvent {
    data object Success : RegisterEvent()

    data class Error(
        val error: RegisterUiError
    ) : RegisterEvent()
}