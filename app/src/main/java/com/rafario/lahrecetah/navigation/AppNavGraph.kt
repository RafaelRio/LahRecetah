package com.rafario.lahrecetah.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rafario.lahrecetah.ui.login.LoginScreen
import com.rafario.lahrecetah.ui.main.MainScreen
import com.rafario.lahrecetah.ui.recipe_detail.RecipeDetailScreen
import com.rafario.lahrecetah.ui.register.RegisterScreen
import com.rafario.lahrecetah.ui.splash.SplashScreen

@Composable
fun AppNavGraph(
    navHostController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navHostController,
        startDestination = Routes.SPLASH
    ) {

        composable(Routes.SPLASH) {
            SplashScreen(navHostController)
        }

        composable(Routes.LOGIN) {
            LoginScreen(navHostController = navHostController)
        }

        composable(Routes.REGISTER) {
            RegisterScreen(navHostController = navHostController)
        }

        composable(Routes.MAIN) {
            MainScreen(navHostController = navHostController)
        }

        composable("${Routes.RECIPE_DETAIL}/{recipeId}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("recipeId") ?: return@composable
            RecipeDetailScreen(recipeId = id, navHostController = navHostController)
        }
    }
}

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val MAIN = "main_screen"
    const val RECIPE_DETAIL = "recipe_detail"
}