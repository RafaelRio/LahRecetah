package com.rafario.lahrecetah.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.rafario.lahrecetah.navigation.Routes

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    navHostController: NavHostController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.logoutEvent.collect {
            navHostController.navigate(Routes.LOGIN) {
                popUpTo(Routes.MAIN) { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Perfil", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { viewModel.logout() },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cerrar sesión")
        }
    }
}