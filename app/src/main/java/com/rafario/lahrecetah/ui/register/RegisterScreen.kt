package com.rafario.lahrecetah.ui.register

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.rafario.lahrecetah.ui.custom_views.CustomOutlineTextField

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RegisterScreen(
    modifier: Modifier = Modifier,
    viewModel: RegisterViewModel = hiltViewModel(),
    navHostController: NavHostController,
    onDismiss: () -> Unit = {}
) {

    val name by viewModel.name.collectAsStateWithLifecycle()
    val email by viewModel.email.collectAsStateWithLifecycle()
    val password by viewModel.password.collectAsStateWithLifecycle()
    val registerEvent = viewModel.registerEvent
    val snackbarHostState = remember { SnackbarHostState() }


    LaunchedEffect(registerEvent) {
        registerEvent.collect {
            when (it) {
                RegisterEvent.Success -> {
                    snackbarHostState.showSnackbar(
                        "Revisa tu correo para confirmar la cuenta"
                    )
                    viewModel.clearFields()
                    onDismiss()
                }

                is RegisterEvent.Error -> {
                    snackbarHostState.showSnackbar(
                        it.message ?: "Error desconocido"
                    )
                }
            }
        }
    }
    Box {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {

            Text("Registro", style = MaterialTheme.typography.displayMedium)
            CustomOutlineTextField(
                value = name,
                onValueChange = { viewModel.onNameChanged(it) },
                label = "Nombre",
                modifier = Modifier.padding(top = 10.dp)
            )

            CustomOutlineTextField(
                value = email,
                onValueChange = { viewModel.onEmailChanged(it) },
                label = "Email",
                modifier = Modifier.padding(top = 10.dp)
            )

            CustomOutlineTextField(
                value = password,
                onValueChange = { viewModel.onPasswordChanged(it) },
                label = "Contraseña",
                modifier = Modifier.padding(top = 10.dp)
            )

            Button(
                onClick = {
                    viewModel.register()
                }, modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 21.dp)
            ) {
                Text("Registrarse")
            }
        }
        SnackbarHost(
            hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}