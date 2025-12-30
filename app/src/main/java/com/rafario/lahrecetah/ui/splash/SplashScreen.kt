package com.rafario.lahrecetah.ui.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import com.rafario.lahrecetah.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navHostController: NavHostController, modifier: Modifier = Modifier) {
    LaunchedEffect(Unit) {
        delay(2000)
        navHostController.navigate("login") {
            popUpTo("splash") { inclusive = true }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(painter = painterResource(id = R.drawable.playstore), contentDescription = "Logo")
        Text(stringResource(R.string.app_name), style = MaterialTheme.typography.displaySmall)
    }
}