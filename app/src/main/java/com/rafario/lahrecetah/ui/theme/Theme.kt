package com.rafario.lahrecetah.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(

    // === COLORES DE MARCA ===
    primary = Primary.copy(alpha = 0.9f),
    onPrimary = Color(0xFF1E1E1E),

    secondary = Secondary.copy(alpha = 0.9f),
    onSecondary = Color(0xFF1E1E1E),

    tertiary = Tertiary.copy(alpha = 0.9f),
    onTertiary = Color(0xFF1E1E1E),

    // === FONDOS ===
    background = Color(0xFF121212),
    onBackground = Color(0xFFEAEAEA),

    surface = Color(0xFF1C1C1C),
    onSurface = Color(0xFFEAEAEA),

    surfaceVariant = Color(0xFF2A2A2A),
    onSurfaceVariant = Color(0xFFD6D6D6),

    // === ESTADOS ===
    error = Error,
    onError = OnError
)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,

    secondary = Secondary,
    onSecondary = OnSecondary,

    tertiary = Tertiary,
    onTertiary = OnTertiary,

    background = AppBackground,
    onBackground = OnSecondary, // texto sobre fondo claro

    surface = Surface,
    onSurface = OnSecondary,

    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSecondary,

    error = Error,
    onError = OnError
)

@Composable
fun LahRecetahTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}