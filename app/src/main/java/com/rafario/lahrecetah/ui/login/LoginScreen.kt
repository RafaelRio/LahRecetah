package com.rafario.lahrecetah.ui.login

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.SecureFlagPolicy
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.rafario.lahrecetah.R
import com.rafario.lahrecetah.ui.custom_views.CustomOutlineTextField
import com.rafario.lahrecetah.ui.register.RegisterScreen
import com.rafario.lahrecetah.ui.theme.ModalBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel(),
    navHostController: NavHostController
) {
    val email by viewModel.email.collectAsStateWithLifecycle()
    val password by viewModel.password.collectAsStateWithLifecycle()
    val rememberMe by viewModel.rememberMe.collectAsStateWithLifecycle()
    val errorMessage by viewModel.error.collectAsStateWithLifecycle()
    val loginEvent = viewModel.loginEvent
    val context = LocalContext.current
    var goToRegister by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(loginEvent) {
        loginEvent.collect { success ->
            if (success) {
                Toast.makeText(context, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Inicio de sesión fallido", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(painter = painterResource(R.drawable.playstore), contentDescription = "Logo")
        Text("Iniciar sesión", style = MaterialTheme.typography.displayMedium)

        Spacer(Modifier.height(16.dp))

        CustomOutlineTextField(
            value = email,
            onValueChange = {
                viewModel.onEmailChanged(it)
            },
            label = "Correo"
        )

        Spacer(Modifier.height(8.dp))

        CustomOutlineTextField(
            value = password,
            onValueChange = {
                viewModel.onPasswordChanged(it)
            },
            label = "Contraseña",
            visualTransformation = PasswordVisualTransformation()
        )


        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = rememberMe,
                onCheckedChange = { viewModel.onRememberMeChanged(it) }
            )
            Text("Recordarme")
            Spacer(Modifier.weight(1f))
        }

        Spacer(Modifier.height(8.dp))

        Button(onClick = {
            viewModel.login()
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Iniciar sesión")
        }


        TextButton(onClick = {
            goToRegister = true
        }, content = {
            Text("Registrarse")
        })

        if (goToRegister) {
            ModalBottomSheet(
                onDismissRequest = {
                    goToRegister = false
                }, 
                sheetState = sheetState, 
                containerColor = ModalBackground,
                properties = ModalBottomSheetProperties(
                    securePolicy = SecureFlagPolicy.SecureOn,
                    shouldDismissOnBackPress = false
                )
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                ) {
                    RegisterScreen(
                        navHostController = navHostController,
                        onDismiss = { goToRegister = false })
                }
            }
        }
    }
}