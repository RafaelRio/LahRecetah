package com.rafario.lahrecetah.ui.register

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rafario.lahrecetah.R
import com.rafario.lahrecetah.ui.custom_views.CustomOutlineTextField

@Composable
fun RegisterScreen(
    modifier: Modifier = Modifier,
    viewModel: RegisterViewModel = hiltViewModel(),
    onDismiss: () -> Unit = {}
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val snackbarHostState = remember {
        SnackbarHostState()
    }

    LaunchedEffect(Unit) {
        viewModel.registerEvent.collect { event ->
            when (event) {
                RegisterEvent.Success -> {
                    snackbarHostState.showSnackbar(
                        context.getString(R.string.verify_email_notice)
                    )

                    onDismiss()
                }

                is RegisterEvent.Error -> {
                    snackbarHostState.showSnackbar(
                        event.message ?: context.getString(R.string.unknown_error)
                    )
                }
            }
        }
    }

    Box(
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            Text(
                text = stringResource(R.string.registration),
                style = MaterialTheme.typography.displayMedium
            )

            CustomOutlineTextField(
                value = uiState.name,
                onValueChange = viewModel::onNameChanged,
                label = stringResource(R.string.name),
                modifier = Modifier.padding(top = 10.dp)
            )

            CustomOutlineTextField(
                value = uiState.email,
                onValueChange = viewModel::onEmailChanged,
                label = stringResource(R.string.email),
                modifier = Modifier.padding(top = 10.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email
                )
            )

            CustomOutlineTextField(
                value = uiState.password,
                onValueChange = viewModel::onPasswordChanged,
                label = stringResource(R.string.password),
                modifier = Modifier.padding(top = 10.dp),
                isPassword = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                )
            )

            Button(
                onClick = viewModel::register,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 21.dp),
                enabled = !uiState.isLoading
            ) {
                Text(stringResource(R.string.sign_up))
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Color.Black.copy(alpha = 0.3f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}
