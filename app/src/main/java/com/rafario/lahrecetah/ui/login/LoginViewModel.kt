package com.rafario.lahrecetah.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.AuthCredential
import com.rafario.lahrecetah.domain.model.UserProfile
import com.rafario.lahrecetah.domain.usecase.GoogleLoginUseCase
import com.rafario.lahrecetah.domain.usecase.LoginUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUserUseCase: LoginUserUseCase,
    private val loginWithGoogleUseCase: GoogleLoginUseCase
) : ViewModel() {

    private val _email = MutableStateFlow("")
    var email = _email.asStateFlow()
    private val _password = MutableStateFlow("")
    var password = _password.asStateFlow()
    private val _rememberMe = MutableStateFlow(false)
    var rememberMe = _rememberMe.asStateFlow()
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()
    private val _loginEvent = MutableSharedFlow<LoginEvent>()
    val loginEvent = _loginEvent.asSharedFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun login() {
        viewModelScope.launch {
            if (_email.value.isBlank() || _password.value.isBlank()) {
                _loginEvent.emit(LoginEvent.Error("Email y contraseña obligatorios"))
                return@launch
            }

            _isLoading.value = true

            val result = loginUserUseCase(_email.value.trim(), _password.value)
            if (result.isSuccess) {
                val authUser = result.getOrNull()
                if (authUser != null) {
                    val userName = authUser.displayName ?: _email.value.split("@").first()
                    _loginEvent.emit(
                        LoginEvent.Success(
                            user = UserProfile(
                                uid = authUser.uid,
                                name = userName,
                                email = authUser.email
                            )
                        )
                    )
                } else {
                    _loginEvent.emit(LoginEvent.Error("Error al obtener datos del usuario"))
                }
            } else {
                _error.value = result.exceptionOrNull()?.message
                _loginEvent.emit(LoginEvent.Error(result.exceptionOrNull()?.message))
            }

            _isLoading.value = false // ✅ Termina loader
        }
    }

    fun loginWithGoogle(credential: AuthCredential) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = loginWithGoogleUseCase(credential)
            if (result.isSuccess) {
                val authUser = result.getOrNull()
                if (authUser != null) {
                    val userName = authUser.displayName ?: "Usuario"
                    _loginEvent.emit(
                        LoginEvent.Success(
                            user = UserProfile(
                                uid = authUser.uid,
                                name = userName,
                                email = authUser.email
                            )
                        )
                    )
                } else {
                    _loginEvent.emit(LoginEvent.Error("Error al obtener datos del usuario"))
                }
            } else {
                _error.value = result.exceptionOrNull()?.message
                _loginEvent.emit(LoginEvent.Error(result.exceptionOrNull()?.message))
            }

            _isLoading.value = false
        }
    }

    fun onEmailChanged(newEmail: String) {
        _email.value = newEmail
    }

    fun onPasswordChanged(newPassword: String) {
        _password.value = newPassword
    }

    fun onRememberMeChanged(newValue: Boolean) {
        _rememberMe.value = newValue
    }
}

sealed class LoginEvent {
    data class Success(val user: UserProfile) : LoginEvent()
    data class Error(val message: String?) : LoginEvent()
}