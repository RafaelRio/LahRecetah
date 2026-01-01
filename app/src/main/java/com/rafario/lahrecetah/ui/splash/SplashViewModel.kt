package com.rafario.lahrecetah.ui.splash

import androidx.lifecycle.ViewModel
import com.rafario.lahrecetah.data.repository.AuthRepository
import com.rafario.lahrecetah.domain.usecase.users.GetRememberMeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    getRememberMeUseCase: GetRememberMeUseCase, authRepository: AuthRepository
) : ViewModel() {

    val startDestination: Flow<String> = combine(
        getRememberMeUseCase(), flowOf(authRepository.getCurrentUser())
    ) { rememberMe, user ->
        if (rememberMe && user != null) {
            "main_screen"
        } else {
            "login"
        }
    }
}