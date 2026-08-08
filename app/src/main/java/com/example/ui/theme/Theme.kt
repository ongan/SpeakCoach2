package com.example.ui.theme

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
    primary = ActiveCyan,
    onPrimary = DeepNavy,
    primaryContainer = DeepNavyContainer,
    onPrimaryContainer = Color.White,
    secondary = ActiveCyan,
    onSecondary = DeepNavy,
    tertiary = CoachAmber,
    onTertiary = DeepNavy,
    background = DeepNavy,
    onBackground = Color.White,
    surface = DeepNavyContainer,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF383D31),
    onSurfaceVariant = OutlineVariant,
    outline = OutlineColor
)

private val LightColorScheme = lightColorScheme(
    primary = DeepNavy,
    onPrimary = Color.White,
    primaryContainer = DeepNavyContainer,
    onPrimaryContainer = SurfaceLow,
    secondary = Color(0xFF6B705C),
    onSecondary = Color.White,
    secondaryContainer = SurfaceContainer,
    onSecondaryContainer = OnSurfacePrimary,
    tertiary = CoachAmber,
    onTertiary = Color.White,
    tertiaryContainer = CoachAmberContainer,
    onTertiaryContainer = OnSurfacePrimary,
    background = SoftBackground,
    onBackground = OnSurfacePrimary,
    surface = Color.White,
    onSurface = OnSurfacePrimary,
    surfaceVariant = SurfaceLow,
    onSurfaceVariant = OnSurfaceVariant,
    outline = OutlineColor,
    outlineVariant = OutlineVariant,
    error = ErrorSoftRed,
    onError = Color.White
)

@Composable
fun SpeakCoachTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
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
        typography = Typography,
        content = content
    )
}

