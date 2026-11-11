@file:Suppress("MagicNumber") // Palette file: hex colour literals are the tokens themselves.

package com.kshrd.coreui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * WebBlock colour tokens. Light = the web dashboard's Figma variables ("Local-WebBlock-UI":
 * `--color-bg-brand`, `--color-text-primary`, `--color-border-subtle`, …); dark = adaptations of the same hues.
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
    /** Page canvas behind screens (web `--color-bg-page` / dark navy); white cards sit on it. */
    val canvas: Color get() = background

    /** Solid button / selected-chip blue (web `--color-bg-brand`, both modes). */
    val buttonBlue: Color get() = Color(0xFF2F6BC4)

    /** Diagonal brand blue behind white initials and icons (logo fallbacks, avatars, progress fills). Same in both modes. */
    val brandGradient: Brush get() = Brush.linearGradient(BrandGradientColors)
}

/** The two stops of [WbColors.brandGradient], for callers that need another direction. */
val BrandGradientColors = listOf(Color(0xFF3C78CF), Color(0xFF2A5FBD))

internal val LightWbColors =
    WbColors(
        brand = Color(0xFF2F6BC4), // --color-bg-brand / --color-text-brand
        brandDeep = Color(0xFF2A5FBD), // --color-text-brand-strong
        background = Color(0xFFF8F9FC), // --color-bg-page
        card = Color(0xFFFFFFFF), // --color-bg-surface
        text = Color(0xFF1B2B4B), // --color-text-primary
        muted = Color(0xFF5A6B8C), // --color-text-muted
        border = Color(0xFFE5E7EB), // --color-border-subtle
        brandSoft = Color(0xFFE3EEFC), // --color-bg-brand-soft
        soft = Color(0xFFEEF4FD), // --color-bg-subtle
        green = Color(0xFF1E8A5A), // --color-text-success
        greenSoft = Color(0xFFE6F5EC), // --color-bg-success-soft
        amber = Color(0xFFB76E0B), // --color-text-warning
        amberSoft = Color(0xFFFDF1DE), // --color-bg-warning-soft
        red = Color(0xFFCC3D3D), // --color-text-danger
        redSoft = Color(0xFFFCE8E8), // --color-bg-danger-soft
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
