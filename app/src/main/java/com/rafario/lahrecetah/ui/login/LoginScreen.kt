package com.rafario.lahrecetah.ui.login

import android.content.MutableContextWrapper
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.SecureFlagPolicy
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.rafario.lahrecetah.R
import com.rafario.lahrecetah.ui.custom_views.CustomOutlineTextField
import com.rafario.lahrecetah.ui.register.RegisterScreen
import com.rafario.lahrecetah.ui.theme.ModalBackground
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var goToRegister by rememberSaveable {
        mutableStateOf(false)
    }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    val snackbarHostState = remember {
        SnackbarHostState()
    }
    val googleSignInHandler = remember(context) {
        GoogleSignInHandler.create(context)
    }

    val scope = rememberCoroutineScope()

    val googleClientId =
        stringResource(R.string.default_web_client_id)

    var isSelectingGoogleAccount by remember {
        mutableStateOf(false)
    }

    val isBusy =
        uiState.isLoading || isSelectingGoogleAccount


    fun startGoogleSignIn() {
        if (isBusy) return

        isSelectingGoogleAccount = true

        scope.launch {
            try {
                when (
                    val result = googleSignInHandler.signIn(
                        context = context,
                        serverClientId = googleClientId
                    )
                ) {
                    is GoogleSignInResult.Success -> {
                        viewModel.loginWithGoogle(
                            result.idToken
                        )
                    }

                    GoogleSignInResult.Cancelled -> {
                        // El usuario cerró el selector.
                        // No es un error.
                    }

                    GoogleSignInResult.NoCredential -> {
                        Toast.makeText(
                            context,
                            context.getString(
                                R.string.google_account_unavailable
                            ),
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    is GoogleSignInResult.Error -> {
                        Toast.makeText(
                            context,
                            context.getString(
                                R.string.google_sign_in_error,
                                result.cause.message
                            ),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } finally {
                isSelectingGoogleAccount = false
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loginEvent.collect { event ->
            when (event) {
                LoginEvent.Success -> {
                    onLoginSuccess()
                }

                is LoginEvent.Error -> {
                    snackbarHostState.showSnackbar(
                        event.message ?: context.getString(R.string.error)
                    )
                }
            }
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 21.dp)
                .verticalScroll(rememberScrollState())
                .padding(
                    bottom = WindowInsets.ime
                        .asPaddingValues()
                        .calculateBottomPadding()
                ),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.playstore),
                contentDescription = stringResource(R.string.app_logo)
            )

            Text(
                text = stringResource(R.string.login),
                style = MaterialTheme.typography.displayMedium
            )

            Spacer(
                Modifier.height(16.dp)
            )

            CustomOutlineTextField(
                value = uiState.email,
                onValueChange = viewModel::onEmailChanged,
                label = stringResource(R.string.email_label),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email
                )
            )

            Spacer(
                Modifier.height(8.dp)
            )

            CustomOutlineTextField(
                value = uiState.password,
                onValueChange = viewModel::onPasswordChanged,
                label = stringResource(R.string.password),
                isPassword = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                )
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = uiState.rememberMe,
                    onCheckedChange = viewModel::onRememberMeChanged
                )

                Text(stringResource(R.string.remember_me))

                Spacer(
                    Modifier.weight(1f)
                )
            }

            Spacer(
                Modifier.height(8.dp)
            )

            Button(
                onClick = viewModel::login,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isBusy
            ) {
                Text(stringResource(R.string.login))
            }

            Spacer(
                Modifier.height(4.dp)
            )

            OutlinedButton(
                onClick = {
                    startGoogleSignIn()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isBusy,
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White
                )
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_google),
                    contentDescription = stringResource(R.string.google_logo),
                    modifier = Modifier.size(20.dp)
                )

                Spacer(
                    Modifier.padding(horizontal = 8.dp)
                )

                Text(
                    text = stringResource(R.string.continue_with_google),
                    color = Color.Black
                )
            }

            TextButton(
                onClick = {
                    goToRegister = true
                },
                enabled = !isBusy
            ) {
                Text(stringResource(R.string.sign_up))
            }

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
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RegisterScreen(
                            onDismiss = {
                                goToRegister = false
                            }
                        )
                    }
                }
            }
        }

        if (isBusy) {
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
