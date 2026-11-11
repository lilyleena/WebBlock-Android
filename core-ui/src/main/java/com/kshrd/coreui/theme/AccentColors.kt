@file:Suppress("MagicNumber")

package com.kshrd.coreui.theme

import androidx.compose.ui.graphics.Color

/**
 * Extra accents for charts and metric tiles, next to the brand blue and the status colours in the
 * colour scheme. Fixed in light and dark; use the translucent container for backgrounds.
 */
object AccentColors {
    val Amber = Color(0xFFF59E0B)
    val AmberContainer = Amber.copy(alpha = 0.16f)
}
