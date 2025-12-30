package com.rafario.lahrecetah.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val loginUserUseCase: LoginUserUseCase
): ViewModel() {

    private val _email= MutableStateFlow("")
    var email = _email.asStateFlow()
    private val _password= MutableStateFlow("")
    var password = _password.asStateFlow()
    private val _rememberMe= MutableStateFlow(false)
    var rememberMe = _rememberMe.asStateFlow()
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()
    private val _loginEvent = MutableSharedFlow<Boolean>()
    val loginEvent = _loginEvent.asSharedFlow()

    fun login() {
        viewModelScope.launch {
            val result = loginUserUseCase(_email.value, _password.value)
            if (result.isSuccess) {
                _loginEvent.emit(true)
            } else {
                _loginEvent.emit(false)
                _error.value = result.exceptionOrNull()?.message
            }
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