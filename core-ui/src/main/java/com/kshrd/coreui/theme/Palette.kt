@file:Suppress("MagicNumber") // Palette file: hex colour literals are the tokens themselves.

package com.kshrd.coreui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * WebBlock colour tokens, copied 1:1 from the iOS app's `Theme.swift`
 * (light = Figma values, dark = the iOS dark adaptations).
 * Tokens with no Material 3 slot are exposed through [WbColors] (`MaterialTheme.wb`).
 */
@Immutable
class WbColors(
    val brand: Color,
    val brandDeep: Color,
    val background: Color,
    val card: Color,
    val text: Color,
    val muted: Color,
    val border: Color,
    val brandSoft: Color,
    val soft: Color,
    val green: Color,
    val greenSoft: Color,
    val amber: Color,
    val amberSoft: Color,
    val red: Color,
    val redSoft: Color,
    val purple: Color,
    val purpleSoft: Color,
    val isDark: Boolean,
) {
    /** Plain page canvas used behind screens (white / dark navy). */
    val canvas: Color get() = if (isDark) Color(0xFF0B1220) else Color.White

    /** Solid button / selected-chip blue (`0x3F72D0` on iOS, both modes). */
    val buttonBlue: Color get() = Color(0xFF3F72D0)

    /** Diagonal brand blue behind white initials and icons (logo fallbacks, avatars, progress fills). Same in both modes. */
    val brandGradient: Brush get() = Brush.linearGradient(BrandGradientColors)
}

/** The two stops of [WbColors.brandGradient], for callers that need another direction. */
val BrandGradientColors = listOf(Color(0xFF4F86DD), Color(0xFF3A6CCB))

internal val LightWbColors =
    WbColors(
        brand = Color(0xFF4379D6),
        brandDeep = Color(0xFF2F63C0),
        background = Color(0xFFF6F8FD),
        card = Color(0xFFFFFFFF),
        text = Color(0xFF1A273D),
        muted = Color(0xFF657899),
        border = Color(0xFFE1E8F5),
        brandSoft = Color(0xFFE6EEFB),
        soft = Color(0xFFF0F4FB),
        green = Color(0xFF1E9E64),
        greenSoft = Color(0xFFE3F5EC),
        amber = Color(0xFFE59A1A),
        amberSoft = Color(0xFFFDF0D8),
        red = Color(0xFFDC3F45),
        redSoft = Color(0xFFFDE8E8),
        purple = Color(0xFF7C4DCC),
        purpleSoft = Color(0xFFEFE7FB),
        isDark = false,
    )

internal val DarkWbColors =
    WbColors(
        brand = Color(0xFF6E9BEA),
        brandDeep = Color(0xFF4F86DD),
        background = Color(0xFF0B1220),
        card = Color(0xFF131B2E),
        text = Color(0xFFEAF0FB),
        muted = Color(0xFF98A6C4),
        border = Color(0xFF26314B),
        brandSoft = Color(0xFF1D2C4D),
        soft = Color(0xFF18223A),
        green = Color(0xFF4CC38A),
        greenSoft = Color(0xFF12301F),
        amber = Color(0xFFF0B14A),
        amberSoft = Color(0xFF3A2A0C),
        red = Color(0xFFF07A6E),
        redSoft = Color(0xFF3A1616),
        purple = Color(0xFFB195EE),
        purpleSoft = Color(0xFF2A1F45),
        isDark = true,
    )

val LocalWbColors = staticCompositionLocalOf { LightWbColors }
