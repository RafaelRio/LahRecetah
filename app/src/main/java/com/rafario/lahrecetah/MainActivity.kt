package com.rafario.lahrecetah

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.SideEffect
import androidx.core.view.WindowInsetsControllerCompat
import com.rafario.lahrecetah.navigation.AppNavGraph
import com.rafario.lahrecetah.ui.theme.LahRecetahTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val darkTheme = isSystemInDarkTheme()

            LahRecetahTheme {
                SideEffect {
                    WindowInsetsControllerCompat(window, window.decorView)
                        .isAppearanceLightStatusBars = !darkTheme
                }

                AppNavGraph()
            }
        }
    }
}