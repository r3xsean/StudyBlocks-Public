package com.example.studyblocks.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Primary Brand Colors - Blue Family
val StudyBlue = Color(0xFF3B82F6)        // Modern blue
val StudyBlueVariant = Color(0xFF1E40AF)  // Deeper blue
val StudyBlueLight = Color(0xFF93C5FD)    // Light blue
val StudyBlueUltraLight = Color(0xFFDEEAFE) // Ultra light blue

// Success Colors - Green Family  
val StudyGreen = Color(0xFF10B981)        // Modern green
val StudyGreenVariant = Color(0xFF059669) // Deeper green
val StudyGreenLight = Color(0xFF6EE7B7)   // Light green
val StudyGreenUltraLight = Color(0xFFD1FAE5) // Ultra light green

// Warning Colors - Orange/Amber Family
val StudyOrange = Color(0xFFF59E0B)       // Modern amber
val StudyOrangeVariant = Color(0xFFD97706) // Deeper amber
val StudyOrangeLight = Color(0xFFFDE68A)   // Light amber
val StudyOrangeUltraLight = Color(0xFFFEF3C7) // Ultra light amber

// Error Colors - Red Family
val StudyRed = Color(0xFFEF4444)          // Modern red
val StudyRedVariant = Color(0xFFDC2626)   // Deeper red
val StudyRedLight = Color(0xFFFCA5A5)     // Light red
val StudyRedUltraLight = Color(0xFFFEE2E2) // Ultra light red

// Dark Theme Variants
val StudyBlueDark = Color(0xFF60A5FA)
val StudyBlueVariantDark = Color(0xFF3B82F6)
val StudyGreenDark = Color(0xFF34D399)
val StudyGreenVariantDark = Color(0xFF10B981)
val StudyOrangeDark = Color(0xFFFBBF24)
val StudyOrangeVariantDark = Color(0xFFF59E0B)
val StudyRedDark = Color(0xFFF87171)
val StudyRedVariantDark = Color(0xFFEF4444)

// Enhanced Neutral Palette
val BackgroundLight = Color(0xFFFBFBFE)      // Softer background with slight purple tint
val SurfaceLight = Color(0xFFFFFFFF)         // Pure white
val SurfaceVariantLight = Color(0xFFF3F4F6)  // Light gray
val SurfaceElevatedLight = Color(0xFFFFFFFF) // Elevated surface
val SurfaceDimLight = Color(0xFFF9FAFB)      // Dimmed surface

// Glassmorphic surface variants
val GlassSurfaceLight = Color(0xFFFFFFFF).copy(alpha = 0.85f)
val GlassSurfaceVariantLight = Color(0xFFF8F9FA).copy(alpha = 0.90f)
val GlassElevatedLight = Color(0xFFFFFFFF).copy(alpha = 0.95f)

val BackgroundDark = Color(0xFF0F0F0F)       // Deeper dark
val SurfaceDark = Color(0xFF1A1A1A)          // Dark surface
val SurfaceVariantDark = Color(0xFF262626)   // Variant dark
val SurfaceElevatedDark = Color(0xFF202020)  // Elevated dark
val SurfaceDimDark = Color(0xFF171717)       // Dimmed dark

// Glassmorphic dark variants
val GlassSurfaceDark = Color(0xFF1A1A1A).copy(alpha = 0.85f)
val GlassSurfaceVariantDark = Color(0xFF262626).copy(alpha = 0.90f)
val GlassElevatedDark = Color(0xFF2A2A2A).copy(alpha = 0.95f)

// Semantic Colors
val SuccessLight = StudyGreen
val SuccessDark = StudyGreenDark
val WarningLight = StudyOrange
val WarningDark = StudyOrangeDark
val ErrorLight = StudyRed
val ErrorDark = StudyRedDark
val InfoLight = StudyBlue
val InfoDark = StudyBlueDark

// XP and Gamification Colors
val XPGold = Color(0xFFFFD700)
val XPGoldDark = Color(0xFFFFC107)
val LevelBronze = Color(0xFFCD7F32)
val LevelSilver = Color(0xFFC0C0C0)
val LevelGold = Color(0xFFFFD700)
val LevelPlatinum = Color(0xFFE5E4E2)

// Modern accent colors inspired by screenshots
val ModernPurple = Color(0xFF6366F1)         // Indigo purple
val ModernPurpleLight = Color(0xFF818CF8)    // Light purple
val ModernPurpleDark = Color(0xFF4F46E5)     // Deep purple
val ModernPink = Color(0xFFEC4899)           // Modern pink
val ModernPinkLight = Color(0xFFF472B6)      // Light pink
val ModernPinkDark = Color(0xFFDB2777)       // Deep pink
val ModernTeal = Color(0xFF06B6D4)           // Cyan teal
val ModernTealLight = Color(0xFF22D3EE)      // Light teal
val ModernTealDark = Color(0xFF0891B2)       // Deep teal

// Confidence Rating Colors (1-10 scale) - Enhanced with modern palette
val ConfidenceColors = listOf(
    Color(0xFFDC2626), // 1 - Deep red (needs most help)
    Color(0xFFEA580C), // 2 - Orange-red
    Color(0xFFD97706), // 3 - Orange
    Color(0xFFF59E0B), // 4 - Amber
    Color(0xFFEAB308), // 5 - Yellow
    Color(0xFF84CC16), // 6 - Light green
    Color(0xFF22C55E), // 7 - Green
    Color(0xFF10B981), // 8 - Emerald
    ModernTeal,        // 9 - Modern teal
    ModernPurple       // 10 - Modern purple (most confident)
)

// Enhanced confidence gradients
val ConfidenceGradients = listOf(
    Brush.linearGradient(colors = listOf(Color(0xFFDC2626), Color(0xFFEF4444))), // 1
    Brush.linearGradient(colors = listOf(Color(0xFFEA580C), Color(0xFFF97316))), // 2
    Brush.linearGradient(colors = listOf(Color(0xFFD97706), Color(0xFFF59E0B))), // 3
    Brush.linearGradient(colors = listOf(Color(0xFFF59E0B), Color(0xFFFBBF24))), // 4
    Brush.linearGradient(colors = listOf(Color(0xFFEAB308), Color(0xFFFDE047))), // 5
    Brush.linearGradient(colors = listOf(Color(0xFF84CC16), Color(0xFFA3E635))), // 6
    Brush.linearGradient(colors = listOf(Color(0xFF22C55E), Color(0xFF4ADE80))), // 7
    Brush.linearGradient(colors = listOf(Color(0xFF10B981), Color(0xFF34D399))), // 8
    Brush.linearGradient(colors = listOf(ModernTeal, ModernTealLight)),           // 9
    Brush.linearGradient(colors = listOf(ModernPurple, ModernPurpleLight))       // 10
)

// Gradients for Modern UI
object StudyGradients {
    val primaryGradient = Brush.linearGradient(
        colors = listOf(StudyBlue, StudyBlueVariant)
    )
    
    val primaryGradientDark = Brush.linearGradient(
        colors = listOf(StudyBlueDark, StudyBlueVariantDark)
    )
    
    val successGradient = Brush.linearGradient(
        colors = listOf(StudyGreen, StudyGreenVariant)
    )
    
    val successGradientDark = Brush.linearGradient(
        colors = listOf(StudyGreenDark, StudyGreenVariantDark)
    )
    
    val warningGradient = Brush.linearGradient(
        colors = listOf(StudyOrange, StudyOrangeVariant)
    )
    
    val warningGradientDark = Brush.linearGradient(
        colors = listOf(StudyOrangeDark, StudyOrangeVariantDark)
    )
    
    val xpGradient = Brush.linearGradient(
        colors = listOf(XPGold, StudyOrange)
    )
    
    val surfaceGradient = Brush.linearGradient(
        colors = listOf(SurfaceLight, SurfaceVariantLight)
    )
    
    val surfaceGradientDark = Brush.linearGradient(
        colors = listOf(SurfaceDark, SurfaceVariantDark)
    )
    
    // Modern gradient collection inspired by screenshots
    val purplePinkGradient = Brush.linearGradient(
        colors = listOf(ModernPurple, ModernPink)
    )
    
    val purpleTealGradient = Brush.linearGradient(
        colors = listOf(ModernPurple, ModernTeal)
    )
    
    val pinkTealGradient = Brush.linearGradient(
        colors = listOf(ModernPink, ModernTeal)
    )
    
    // Glassmorphic gradients
    val glassPrimaryGradient = Brush.linearGradient(
        colors = listOf(
            StudyBlue.copy(alpha = 0.15f),
            StudyBlueVariant.copy(alpha = 0.25f)
        )
    )
    
    val glassSuccessGradient = Brush.linearGradient(
        colors = listOf(
            StudyGreen.copy(alpha = 0.15f),
            StudyGreenVariant.copy(alpha = 0.25f)
        )
    )
    
    val glassPurpleGradient = Brush.linearGradient(
        colors = listOf(
            ModernPurple.copy(alpha = 0.15f),
            ModernPurpleDark.copy(alpha = 0.25f)
        )
    )
    
    val glassPinkGradient = Brush.linearGradient(
        colors = listOf(
            ModernPink.copy(alpha = 0.15f),
            ModernPinkDark.copy(alpha = 0.25f)
        )
    )
    
    // Celebration gradients
    val celebrationGradient = Brush.radialGradient(
        colors = listOf(
            XPGold.copy(alpha = 0.8f),
            StudyOrange.copy(alpha = 0.6f),
            ModernPink.copy(alpha = 0.4f)
        )
    )
    
    val achievementGradient = Brush.linearGradient(
        colors = listOf(
            ModernPurple,
            ModernPink,
            XPGold
        )
    )
}

// Legacy colors for compatibility
val Purple80 = StudyBlueDark
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = StudyOrangeDark

val Purple40 = StudyBlue
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = StudyOrange