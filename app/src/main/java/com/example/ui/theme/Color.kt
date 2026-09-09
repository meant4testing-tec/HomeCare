package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Primary Brand Palette: Bright Azure Sky & Vivid Solar Amber
val TealPrimaryLight = Color(0xFF0284C7)        // Bright Electric Sky Azure
val TealOnPrimaryLight = Color(0xFFFFFFFF)
val TealContainerLight = Color(0xFFE0F2FE)       // Bright Luminous Sky Tint
val TealOnContainerLight = Color(0xFF0369A1)

val AmberSecondaryLight = Color(0xFFEA580C)      // Bright Tangerine / Coral Amber
val AmberOnSecondaryLight = Color(0xFFFFFFFF)
val AmberContainerLight = Color(0xFFFFEDD5)      // Warm radiant peach
val AmberOnContainerLight = Color(0xFF9A3412)

val SurfaceLight = Color(0xFFFFFFFF)             // Crisp clean white cards
val OnSurfaceLight = Color(0xFF0F172A)           // High contrast deep slate
val SurfaceVariantLight = Color(0xFFF1F5F9)      // Soft clean light slate
val OnSurfaceVariantLight = Color(0xFF334155)
val CardBackgroundLight = Color(0xFFFFFFFF)
val AppBackgroundLight = Color(0xFFF8FAFC)        // Bright radiant canvas

// Urgency Colors - Vibrant & High Contrast
val OverdueRedLight = Color(0xFFEF4444)          // Vibrant Coral Red
val OverdueBgLight = Color(0xFFFEF2F2)
val OverdueBorderLight = Color(0xFFFECACA)

val DueTodayAmberLight = Color(0xFFF59E0B)       // Sunny Vibrant Amber
val DueTodayBgLight = Color(0xFFFFFBEB)
val DueTodayBorderLight = Color(0xFFFDE68A)

val DueSoonBlueLight = Color(0xFF3B82F6)         // Radiant Bright Royal Blue
val DueSoonBgLight = Color(0xFFEFF6FF)
val DueSoonBorderLight = Color(0xFFBFDBFE)

val CompletedGreenLight = Color(0xFF10B981)      // Fresh Vivid Mint Emerald
val CompletedBgLight = Color(0xFFECFDF5)
val CompletedBorderLight = Color(0xFFA7F3D0)

// Vibrant Category Colors for chips, badges, and icons
object CategoryPalette {
    data class CategoryColorSet(
        val iconColor: Color,
        val containerColor: Color,
        val borderColor: Color
    )

    fun forCategory(category: String): CategoryColorSet = when (category) {
        "HVAC & Cooling" -> CategoryColorSet(
            iconColor = Color(0xFF0284C7),
            containerColor = Color(0xFFE0F2FE),
            borderColor = Color(0xFFBAE6FD)
        )
        "Kitchen Appliances" -> CategoryColorSet(
            iconColor = Color(0xFFEA580C),
            containerColor = Color(0xFFFFEDD5),
            borderColor = Color(0xFFFED7AA)
        )
        "Major Appliances" -> CategoryColorSet(
            iconColor = Color(0xFF0D9488),
            containerColor = Color(0xFFCCFBF1),
            borderColor = Color(0xFF99F6E4)
        )
        "Vehicles" -> CategoryColorSet(
            iconColor = Color(0xFF6366F1),
            containerColor = Color(0xFFEEF2FF),
            borderColor = Color(0xFFC7D2FE)
        )
        "Plumbing & Water" -> CategoryColorSet(
            iconColor = Color(0xFF06B6D4),
            containerColor = Color(0xFFECFEFF),
            borderColor = Color(0xFFA5F3FC)
        )
        "Electrical & Backup" -> CategoryColorSet(
            iconColor = Color(0xFFD97706),
            containerColor = Color(0xFFFEF3C7),
            borderColor = Color(0xFFFDE68A)
        )
        "Home Care & Safety" -> CategoryColorSet(
            iconColor = Color(0xFF16A34A),
            containerColor = Color(0xFFDCFCE7),
            borderColor = Color(0xFFBBF7D0)
        )
        "Garden & Outdoor" -> CategoryColorSet(
            iconColor = Color(0xFF65A30D),
            containerColor = Color(0xFFECFCCB),
            borderColor = Color(0xFFD9F99D)
        )
        else -> CategoryColorSet(
            iconColor = Color(0xFF8B5CF6),
            containerColor = Color(0xFFF3E8FF),
            borderColor = Color(0xFFDDD6FE)
        )
    }
}

// Dark Palette
val TealPrimaryDark = Color(0xFF5EEAD4)
val TealOnPrimaryDark = Color(0xFF00373F)
val TealContainerDark = Color(0xFF004F5A)
val TealOnContainerDark = Color(0xFFCCEDF4)

val AmberSecondaryDark = Color(0xFFFFB870)
val AmberOnSecondaryDark = Color(0xFF4B2800)
val AmberContainerDark = Color(0xFF6B3D00)
val AmberOnContainerDark = Color(0xFFFFDDB8)

val SurfaceDark = Color(0xFF0B131D)
val OnSurfaceDark = Color(0xFFE2E8F0)
val SurfaceVariantDark = Color(0xFF1E293B)
val OnSurfaceVariantDark = Color(0xFF94A3B8)
val CardBackgroundDark = Color(0xFF131F30)

val OverdueRedDark = Color(0xFFF87171)
val OverdueBgDark = Color(0xFF450A0A)

val DueTodayAmberDark = Color(0xFFFBBF24)
val DueTodayBgDark = Color(0xFF451A03)

val DueSoonBlueDark = Color(0xFF60A5FA)
val DueSoonBgDark = Color(0xFF172554)

val CompletedGreenDark = Color(0xFF34D399)
val CompletedBgDark = Color(0xFF064E3B)
