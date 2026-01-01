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
    // Si aún no tienes paleta dark, usamos un dark coherente:
    // (cuando quieras te preparo una pastel dark de verdad)
    primary = Primary,
    onPrimary = OnPrimary,

    secondary = Secondary,
    onSecondary = OnSecondary,

    tertiary = Tertiary,
    onTertiary = OnTertiary,

    background = Color(0xFF121212),
    onBackground = Color(0xFFEFEFEF),

    surface = Color(0xFF1A1A1A),
    onSurface = Color(0xFFEFEFEF),

    surfaceVariant = Color(0xFF242424),
    onSurfaceVariant = Color(0xFFE0E0E0),

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