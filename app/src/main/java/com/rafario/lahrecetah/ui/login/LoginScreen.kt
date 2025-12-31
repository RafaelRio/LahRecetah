package com.rafario.lahrecetah.ui.login

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.SecureFlagPolicy
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
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
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val loginEvent = viewModel.loginEvent
    val context = LocalContext.current
    var goToRegister by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val snackbarHostState = remember { SnackbarHostState() }


    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id)).requestEmail()
            .build()
    }
    val googleSignInClient = remember {
        GoogleSignIn.getClient(context, gso)
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            viewModel.loginWithGoogle(credential)
        } catch (e: ApiException) {
            Toast.makeText(
                context, "Error al iniciar sesión con Google: ${e.message}", Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun startGoogleSignIn() {
        googleSignInClient.signOut().addOnCompleteListener {
            launcher.launch(googleSignInClient.signInIntent)
        }
    }

    LaunchedEffect(loginEvent) {
        loginEvent.collect { event ->
            when (event) {
                is LoginEvent.Success -> {
                    navHostController.navigate("main_screen")
                }

                is LoginEvent.Error -> {
                    snackbarHostState.showSnackbar("${event.message}")
                }
            }
        }
    }

    Scaffold(snackbarHost = {
        SnackbarHost(hostState = snackbarHostState)
    }) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 21.dp)
                .verticalScroll(rememberScrollState())
                .padding(
                    bottom = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
                ),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(painter = painterResource(R.drawable.playstore), contentDescription = "Logo")
            Text("Iniciar sesión", style = MaterialTheme.typography.displayMedium)

            Spacer(Modifier.height(16.dp))

            CustomOutlineTextField(
                value = email, onValueChange = {
                    viewModel.onEmailChanged(it)
                }, label = "Correo"
            )

            Spacer(Modifier.height(8.dp))

            CustomOutlineTextField(
                value = password, onValueChange = {
                    viewModel.onPasswordChanged(it)
                }, label = "Contraseña", isPassword = true
            )


            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = rememberMe, onCheckedChange = { viewModel.onRememberMeChanged(it) })
                Text("Recordarme")
                Spacer(Modifier.weight(1f))
            }

            Spacer(Modifier.height(8.dp))

            Button(onClick = {
                viewModel.login()
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Iniciar sesión")
            }

            Spacer(Modifier.height(4.dp))

            OutlinedButton(
                onClick = {
                    startGoogleSignIn()
                }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White
                )
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_google),
                    contentDescription = "Google Logo",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.padding(horizontal = 8.dp))
                Text("Continuar con Google", color = Color.Black)
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
                        securePolicy = SecureFlagPolicy.SecureOn, shouldDismissOnBackPress = false
                    )
                ) {
                    Box(
                        Modifier.fillMaxWidth()
                    ) {
                        RegisterScreen(
                            navHostController = navHostController,
                            onDismiss = { goToRegister = false })
                    }
                }
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}