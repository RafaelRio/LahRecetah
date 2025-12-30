package com.rafario.lahrecetah.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rafario.lahrecetah.ui.login.LoginScreen
import com.rafario.lahrecetah.ui.register.RegisterScreen
import com.rafario.lahrecetah.ui.splash.SplashScreen

@Composable
fun AppNavGraph(navHostController: NavHostController = rememberNavController()) {
    NavHost(navController = navHostController, startDestination = "splash") {
        composable("splash") { SplashScreen(navHostController = navHostController) }
        composable("login") { LoginScreen(navHostController = navHostController) }
        composable("register") { RegisterScreen(navHostController = navHostController) }
    }
}