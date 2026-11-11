package com.kshrd.coreui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

private fun WbColors.toScheme(): ColorScheme =
    if (isDark) {
        darkColorScheme(
            primary = brand,
            onPrimary = Color(0xFF0B1220),
            primaryContainer = brandSoft,
            onPrimaryContainer = text,
            secondary = brandDeep,
            onSecondary = Color.White,
            secondaryContainer = brandSoft,
            onSecondaryContainer = text,
            tertiary = green,
            onTertiary = Color(0xFF0B1220),
            tertiaryContainer = greenSoft,
            onTertiaryContainer = green,
            background = canvas,
            onBackground = text,
            surface = card,
            onSurface = text,
            surfaceVariant = soft,
            onSurfaceVariant = muted,
            surfaceContainerLowest = canvas,
            surfaceContainerLow = background,
            surfaceContainer = soft,
            surfaceContainerHigh = border,
            surfaceContainerHighest = border,
            outline = border,
            outlineVariant = border,
            error = red,
            onError = Color(0xFF0B1220),
            errorContainer = redSoft,
            onErrorContainer = red,
            inverseSurface = text,
            inverseOnSurface = canvas,
            inversePrimary = LightWbColors.brand,
        )
    } else {
        lightColorScheme(
            primary = brand,
            onPrimary = Color.White,
            primaryContainer = brandSoft,
            onPrimaryContainer = text,
            secondary = brandDeep,
            onSecondary = Color.White,
            secondaryContainer = brandSoft,
            onSecondaryContainer = text,
            tertiary = green,
            onTertiary = Color.White,
            tertiaryContainer = greenSoft,
            onTertiaryContainer = green,
            background = canvas,
            onBackground = text,
            surface = card,
            onSurface = text,
            surfaceVariant = soft,
            onSurfaceVariant = muted,
            surfaceContainerLowest = card,
            surfaceContainerLow = background,
            surfaceContainer = soft,
            surfaceContainerHigh = border,
            surfaceContainerHighest = border,
            outline = border,
            outlineVariant = border,
            error = red,
            onError = Color.White,
            errorContainer = redSoft,
            onErrorContainer = red,
            inverseSurface = text,
            inverseOnSurface = Color.White,
            inversePrimary = DarkWbColors.brand,
        )
    }

/** Tokens without a Material slot (brandSoft, soft, border, green/amber/red/purple + soft…). */
val MaterialTheme.wb: WbColors
    @Composable
    @ReadOnlyComposable
    get() = LocalWbColors.current

/**
 * The app theme. Wrap the whole UI in it once (in `MainActivity`) and every preview in it
 * (through `PreviewSurface`). Screens read colours from `MaterialTheme.colorScheme` / `MaterialTheme.wb`,
 * text styles from `MaterialTheme.typography`, and corners from `MaterialTheme.shapes`.
 */
@Composable
fun AppTheme(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val wb = if (isDarkTheme) DarkWbColors else LightWbColors
    CompositionLocalProvider(LocalWbColors provides wb) {
        MaterialTheme(
            colorScheme = wb.toScheme(),
            typography = AppTypography,
            shapes = AppShapes,
            content = content,
        )
    }
}
