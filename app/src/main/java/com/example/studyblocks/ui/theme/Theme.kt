package com.example.studyblocks.ui.theme

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
    primary = StudyBlueDark,
    onPrimary = Color(0xFF000000),
    primaryContainer = StudyBlueVariantDark,
    onPrimaryContainer = Color(0xFFFFFFFF),
    
    secondary = StudyGreenDark,
    onSecondary = Color(0xFF000000),
    secondaryContainer = StudyGreenVariantDark,
    onSecondaryContainer = Color(0xFFFFFFFF),
    
    tertiary = StudyOrangeDark,
    onTertiary = Color(0xFF000000),
    tertiaryContainer = StudyOrangeVariantDark,
    onTertiaryContainer = Color(0xFFFFFFFF),
    
    background = BackgroundDark,
    onBackground = Color(0xFFE5E5E5),
    surface = SurfaceDark,
    onSurface = Color(0xFFE5E5E5),
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = Color(0xFFBDBDBD),
    surfaceTint = StudyBlueDark,
    
    inverseSurface = SurfaceLight,
    inverseOnSurface = Color(0xFF1C1B1F),
    inversePrimary = StudyBlue,
    
    error = StudyRedDark,
    onError = Color(0xFF000000),
    errorContainer = StudyRedVariantDark,
    onErrorContainer = Color(0xFFFFFFFF),
    
    outline = Color(0xFF6B6B6B),
    outlineVariant = Color(0xFF3E3E3E),
    scrim = Color(0xFF000000)
)

private val LightColorScheme = lightColorScheme(
    primary = StudyBlue,
    onPrimary = Color.White,
    primaryContainer = StudyBlueUltraLight,
    onPrimaryContainer = StudyBlueVariant,
    
    secondary = StudyGreen,
    onSecondary = Color.White,
    secondaryContainer = StudyGreenUltraLight,
    onSecondaryContainer = StudyGreenVariant,
    
    tertiary = StudyOrange,
    onTertiary = Color.White,
    tertiaryContainer = StudyOrangeUltraLight,
    onTertiaryContainer = StudyOrangeVariant,
    
    background = BackgroundLight,
    onBackground = Color(0xFF1A1A1A),
    surface = SurfaceLight,
    onSurface = Color(0xFF1A1A1A),
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = Color(0xFF49454F),
    surfaceTint = StudyBlue,
    
    inverseSurface = SurfaceDark,
    inverseOnSurface = Color(0xFFE5E5E5),
    inversePrimary = StudyBlueDark,
    
    error = StudyRed,
    onError = Color.White,
    errorContainer = StudyRedUltraLight,
    onErrorContainer = StudyRedVariant,
    
    outline = Color(0xFF73777F),
    outlineVariant = Color(0xFFC4C7C5),
    scrim = Color(0xFF000000)
)

@Composable
fun StudyBlocksTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disabled to use custom study colors
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