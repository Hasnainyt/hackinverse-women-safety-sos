package com.brokencoders.narisuraksha.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = PrimaryPink,
    onPrimary = Color.White,
    primaryContainer = SoftPinkSurface,
    onPrimaryContainer = TextPrimary,
    secondary = GuardianBlue,
    onSecondary = Color.White,
    secondaryContainer = SoftPinkSurface,
    onSecondaryContainer = TextPrimary,
    background = AppBackground,
    onBackground = TextPrimary,
    surface = CardBackground,
    onSurface = TextPrimary,
    surfaceVariant = SoftPinkSurface,
    onSurfaceVariant = TextSecondary,
    outline = BorderDivider,
    error = EmergencyRed,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimaryPink,
    onPrimary = Color.White,
    primaryContainer = DarkSoftPinkSurface,
    onPrimaryContainer = DarkTextPrimary,
    secondary = GuardianBlue,
    onSecondary = Color.White,
    secondaryContainer = DarkSoftPinkSurface,
    onSecondaryContainer = DarkTextPrimary,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSoftPinkSurface,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorderDivider,
    error = EmergencyRed,
    onError = Color.White
)

@Composable
fun NariSurakshaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
