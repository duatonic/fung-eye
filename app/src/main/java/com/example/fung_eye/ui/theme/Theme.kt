package com.example.fung_eye.ui.theme

import android.app.Activity
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
    primary = ForestGreen, // A more thematic primary color
    secondary = EarthBrown,
    tertiary = MushroomRed,
    background = DarkCharcoal,
    surface = Color(0xFF1C1B1F), // Default dark surface
    onPrimary = CreamyWhite,
    onSecondary = CreamyWhite,
    onTertiary = CreamyWhite,
    onBackground = CreamyWhite,
    onSurface = CreamyWhite,
    primaryContainer = EarthBrown,
    onPrimaryContainer = CreamyWhite,
)

private val LightColorScheme = lightColorScheme(
    primary = ForestGreen,
    secondary = EarthBrown,
    tertiary = MushroomRed,
    background = LightBeige, // A light, earthy background
    surface = CreamyWhite,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = DarkCharcoal,
    onSurface = DarkCharcoal,
    primaryContainer = ForestGreen, // Container for primary elements
    onPrimaryContainer = Color.White, // Text/icons on primaryContainer

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun FungEyeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
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
        typography = Typography,
        content = content
    )
}