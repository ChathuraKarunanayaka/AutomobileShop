package com.example.automobileshopmanagement.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = SurfaceWhite,
    primaryContainer = LightBlue,
    onPrimaryContainer = PrimaryBlue,
    secondary = SecondaryBlue,
    onSecondary = SurfaceWhite,
    tertiary = AccentCyan,
    onTertiary = SurfaceWhite,
    background = BackgroundGray,
    onBackground = TextPrimary,
    surface = SurfaceWhite,
    onSurface = TextPrimary,
    error = ErrorRed,
    onError = SurfaceWhite,
    outline = DividerGray
)

@Composable
fun AutoMobileShopManagementTheme(
    darkTheme: Boolean = false, // Forced to light theme as requested
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // We ignore darkTheme and dynamicColor for now as requested
    val colorScheme = LightColorScheme

    val view = LocalView.current
    val context = LocalContext.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (context.findActivity())?.window
            if (window != null) {
                window.statusBarColor = colorScheme.primary.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = false // White icons on Blue status bar
                insetsController.isAppearanceLightNavigationBars = true // Dark icons on Gray nav bar
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

private fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}
