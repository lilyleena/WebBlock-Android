package com.kshrd.coreui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/**
 * Line height is proportional (`em`), so a Text that overrides `fontSize` (illustrations, pills, tiny captions)
 * keeps tight, iOS-like leading instead of inheriting a fixed sp line height meant for the base size.
 */
@Suppress("DEPRECATION")
private fun style(
    size: Int,
    weight: FontWeight,
    line: Int,
) = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = (line.toFloat() / size).em,
    platformStyle = PlatformTextStyle(includeFontPadding = false),
    lineHeightStyle = LineHeightStyle(alignment = LineHeightStyle.Alignment.Center, trim = LineHeightStyle.Trim.None),
)

/**
 * iOS text styles mapped onto the Material 3 names (sizes in sp so font scale still works):
 * displaySmall = large title 34 bold (the title at the top of a page), headlineLarge = system 28 bold,
 * headlineMedium = 24 bold (headings inside a page or card), headlineSmall/titleLarge = title2 22 bold, titleMedium = headline 17 semibold,
 * titleSmall = subheadline semibold 15, bodyLarge = body 17, bodyMedium = subheadline 15,
 * bodySmall = footnote 13, labelLarge = subheadline semibold 15, labelMedium = caption 12, labelSmall = caption2 11.
 */
val AppTypography =
    Typography(
        displayMedium = style(48, FontWeight.Bold, 56),
        displaySmall = style(34, FontWeight.ExtraBold, 40),
        headlineLarge = style(28, FontWeight.Bold, 34),
        headlineMedium = style(24, FontWeight.Bold, 30),
        headlineSmall = style(22, FontWeight.Bold, 28),
        titleLarge = style(22, FontWeight.Bold, 28),
        titleMedium = style(17, FontWeight.SemiBold, 22),
        titleSmall = style(15, FontWeight.SemiBold, 20),
        bodyLarge = style(17, FontWeight.Normal, 22),
        bodyMedium = style(15, FontWeight.Normal, 20),
        bodySmall = style(13, FontWeight.Normal, 18),
        labelLarge = style(15, FontWeight.SemiBold, 20),
        labelMedium = style(12, FontWeight.Medium, 16),
        labelSmall = style(11, FontWeight.Medium, 14),
    )

/** Long-form reading text (descriptions, messages): a touch smaller than body with looser leading. */
val Typography.paragraph: TextStyle
    get() = bodyLarge.copy(fontSize = 16.sp, lineHeight = 1.5.em, lineBreak = LineBreak.Paragraph)

/** Small bold tracked caption for labels inside a card and eyebrows. Pass the text already in upper case. */
val Typography.overline: TextStyle
    get() = labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
