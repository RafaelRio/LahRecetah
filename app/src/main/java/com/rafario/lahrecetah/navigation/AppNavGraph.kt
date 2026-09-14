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
            SplashScreen(
                onNavigate = { destination ->
                    navHostController.navigate(destination) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navHostController.navigate(Routes.MAIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen()
        }

        composable(Routes.MAIN) {
            MainScreen(
                onRecipeClick = { recipeId ->
                    navHostController.navigate("${Routes.RECIPE_DETAIL}/$recipeId")
                },
                onLogout = {
                    navHostController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.MAIN) { inclusive = true }
                    }
                }
            )
        }

        composable("${Routes.RECIPE_DETAIL}/{recipeId}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("recipeId") ?: return@composable
            RecipeDetailScreen(
                recipeId = id,
                onBack = { navHostController.popBackStack() }
            )
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