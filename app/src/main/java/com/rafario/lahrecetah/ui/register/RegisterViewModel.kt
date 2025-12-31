package com.rafario.lahrecetah.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafario.lahrecetah.domain.usecase.users.RegisterUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUserUseCase: RegisterUserUseCase
) : ViewModel() {
    private val _name = MutableStateFlow("")
    val name = _name.asStateFlow()

    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()

    private val _registerEvent = MutableSharedFlow<RegisterEvent>()
    val registerEvent = _registerEvent.asSharedFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun onNameChanged(value: String) {
        _name.value = value
    }

    fun onEmailChanged(value: String) {
        _email.value = value
    }

    fun onPasswordChanged(value: String) {
        _password.value = value
    }

    fun register() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = registerUserUseCase(_name.value, _email.value, _password.value)

            if (result.isSuccess) {
                _registerEvent.emit(RegisterEvent.Success)
            } else {
                _registerEvent.emit(RegisterEvent.Error(result.exceptionOrNull()?.message))
            }
            _isLoading.value = false
        }
    }

    fun clearFields() {
        _name.value = ""
        _email.value = ""
        _password.value = ""
    }
}

sealed class RegisterEvent {
    object Success : RegisterEvent()
    data class Error(val message: String?) : RegisterEvent()
}