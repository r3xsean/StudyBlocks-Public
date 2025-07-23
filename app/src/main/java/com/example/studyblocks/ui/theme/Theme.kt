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
    onPrimaryContainer = Color(0xFF000000),
    
    secondary = StudyGreenDark,
    onSecondary = Color(0xFF000000),
    secondaryContainer = StudyGreenVariantDark,
    onSecondaryContainer = Color(0xFF000000),
    
    tertiary = StudyOrangeDark,
    onTertiary = Color(0xFF000000),
    tertiaryContainer = Color(0xFF3E2723),
    onTertiaryContainer = StudyOrangeDark,
    
    background = BackgroundDark,
    onBackground = Color(0xFFE1E1E1),
    surface = SurfaceDark,
    onSurface = Color(0xFFE1E1E1),
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = Color(0xFFBDBDBD),
    
    error = StudyRedDark,
    onError = Color(0xFF000000),
    errorContainer = Color(0xFF3E1412),
    onErrorContainer = StudyRedDark,
    
    outline = Color(0xFF757575),
    outlineVariant = Color(0xFF424242)
)

private val LightColorScheme = lightColorScheme(
    primary = StudyBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE3F2FD),
    onPrimaryContainer = StudyBlueVariant,
    
    secondary = StudyGreen,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8F5E8),
    onSecondaryContainer = StudyGreenVariant,
    
    tertiary = StudyOrange,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFF3E0),
    onTertiaryContainer = Color(0xFFE65100),
    
    background = BackgroundLight,
    onBackground = Color(0xFF1C1B1F),
    surface = SurfaceLight,
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = Color(0xFF49454F),
    
    error = StudyRed,
    onError = Color.White,
    errorContainer = Color(0xFFFFEBEE),
    onErrorContainer = Color(0xFFB71C1C),
    
    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFFCAC4D0)
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