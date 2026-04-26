package com.terminplaner.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.terminplaner.data.preferences.ThemePreferences

// Base Personal Palette (Teal)
private val PersonalLightColors = lightColorScheme(
    primary = Color(0xFF006A6A),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF6FF6F6),
    onPrimaryContainer = Color(0xFF002020),
    secondary = Color(0xFF4A6363),
    onSecondary = Color(0xFFFFFFFF),
    surface = Color(0xFFF4FBFA),
    onSurface = Color(0xFF191C1C),
    surfaceVariant = Color(0xFFDAE5E4),
    onSurfaceVariant = Color(0xFF3F4948)
)

private val PersonalDarkColors = darkColorScheme(
    primary = Color(0xFF4DDADA),
    onPrimary = Color(0xFF003737),
    primaryContainer = Color(0xFF004F4F),
    onPrimaryContainer = Color(0xFF6FF6F6),
    surface = Color(0xFF191C1C),
    onSurface = Color(0xFFE0E3E2),
    surfaceVariant = Color(0xFF3F4948),
    onSurfaceVariant = Color(0xFFBEC9C8)
)

// Helper to create a scheme from a primary color
fun getColorScheme(
    darkTheme: Boolean,
    primaryColor: Long
): ColorScheme {
    val base = if (darkTheme) PersonalDarkColors else PersonalLightColors
    val color = Color(primaryColor)
    
    // Create a cohesive M3 look by deriving secondary and tertiary from primary
    return base.copy(
        primary = color,
        primaryContainer = if (darkTheme) color.copy(alpha = 0.3f) else color.copy(alpha = 0.15f),
        onPrimaryContainer = if (darkTheme) Color.White else color,
        
        // Dynamic secondary/tertiary for the "Design 3" feel
        secondary = if (darkTheme) color.copy(alpha = 0.8f) else color.copy(alpha = 0.7f),
        secondaryContainer = if (darkTheme) color.copy(alpha = 0.2f) else color.copy(alpha = 0.1f),
        
        tertiary = if (darkTheme) color.copy(alpha = 0.6f) else color.copy(alpha = 0.5f),
        
        // Update surface colors slightly to match the tint
        surface = if (darkTheme) Color(0xFF191C1C) else color.copy(alpha = 0.02f).compositeOver(Color.White),
        background = if (darkTheme) Color(0xFF191C1C) else color.copy(alpha = 0.02f).compositeOver(Color.White)
    )
}

// Extension to mix colors for subtle tints
private fun Color.compositeOver(background: Color): Color {
    val alpha = this.alpha
    return Color(
        red = this.red * alpha + background.red * (1f - alpha),
        green = this.green * alpha + background.green * (1f - alpha),
        blue = this.blue * alpha + background.blue * (1f - alpha),
        alpha = 1f
    )
}

// Business Indigo/Dark Blue
private val BusinessLightColors = lightColorScheme(
    primary = Color(0xFF4355B9),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFDEE0FF),
    onPrimaryContainer = Color(0xFF00105C),
    secondary = Color(0xFFAF52DE), // Lila accent
    onSecondary = Color(0xFFFFFFFF),
    surface = Color(0xFFFEFBFF),
    onSurface = Color(0xFF1B1B1F)
)

private val BusinessDarkColors = darkColorScheme(
    primary = Color(0xFFBAC3FF),
    onPrimary = Color(0xFF08218A),
    primaryContainer = Color(0xFF293CA0),
    onPrimaryContainer = Color(0xFFDEE0FF),
    secondary = Color(0xFFD0BCFF),
    onSecondary = Color(0xFF381E72),
    surface = Color(0xFF1B1B1F),
    onSurface = Color(0xFFE3E2E6)
)

@Composable
fun TerminplanerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    userStatus: Int = ThemePreferences.STATUS_PERSONAL,
    primaryColor: Long = 0xFF007AFF,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        userStatus == ThemePreferences.STATUS_BUSINESS -> {
            if (darkTheme) BusinessDarkColors else BusinessLightColors
        }
        else -> {
            getColorScheme(darkTheme, primaryColor)
        }
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
