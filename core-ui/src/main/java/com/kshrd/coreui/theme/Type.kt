package com.kshrd.coreui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.kshrd.coreui.R

/** Body, labels and controls: DM Sans (the web dashboard's Body styles). */
val DmSans =
    FontFamily(
        Font(R.font.dm_sans_400, FontWeight.Normal),
        Font(R.font.dm_sans_500, FontWeight.Medium),
        Font(R.font.dm_sans_600, FontWeight.SemiBold),
        Font(R.font.dm_sans_700, FontWeight.Bold),
    )

/** Headings and page titles: Familjen Grotesk (the web dashboard's Heading and Display styles). */
val FamiljenGrotesk =
    FontFamily(
        Font(R.font.familjen_grotesk_500, FontWeight.Medium),
        Font(R.font.familjen_grotesk_600, FontWeight.SemiBold),
        Font(R.font.familjen_grotesk_700, FontWeight.Bold),
    )

/**
 * Line height is proportional (`em`), so a Text that overrides `fontSize` (illustrations, pills, tiny captions)
 * keeps tight leading instead of inheriting a fixed sp line height meant for the base size.
 */
@Suppress("DEPRECATION")
private fun style(
    size: Int,
    weight: FontWeight,
    line: Int,
    family: FontFamily = DmSans,
    tracking: Float = 0f,
) = TextStyle(
    fontFamily = family,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = (line.toFloat() / size).em,
    letterSpacing = tracking.em,
    platformStyle = PlatformTextStyle(includeFontPadding = false),
    lineHeightStyle = LineHeightStyle(alignment = LineHeightStyle.Alignment.Center, trim = LineHeightStyle.Trim.None),
)

private fun heading(
    size: Int,
    line: Int,
    tracking: Float = -0.02f,
) = style(size, FontWeight.SemiBold, line, FamiljenGrotesk, tracking)

/**
 * The web dashboard's type ramp on the Material 3 names (sizes in sp so font scale still works).
 * Headings (display / headline / titleLarge) = Familjen Grotesk SemiBold with slight negative tracking;
 * everything else = DM Sans.
 * displaySmall = page title 32, headlineLarge 28, headlineMedium 24 (headings inside a page or card),
 * headlineSmall/titleLarge 20, titleMedium = 16 semibold (card titles, buttons), titleSmall = 15 semibold,
 * bodyLarge 16, bodyMedium 14, bodySmall 13, labelLarge = 14 semibold, labelMedium 12, labelSmall 11.
 */
val AppTypography =
    Typography(
        displayMedium = heading(44, 48, tracking = -0.025f),
        displaySmall = heading(32, 36),
        headlineLarge = heading(28, 32),
        headlineMedium = heading(24, 28),
        headlineSmall = heading(20, 26, tracking = -0.01f),
        titleLarge = heading(20, 26, tracking = -0.01f),
        titleMedium = style(16, FontWeight.SemiBold, 22),
        titleSmall = style(15, FontWeight.SemiBold, 21),
        bodyLarge = style(16, FontWeight.Normal, 24),
        bodyMedium = style(14, FontWeight.Normal, 21),
        bodySmall = style(13, FontWeight.Normal, 19),
        labelLarge = style(14, FontWeight.SemiBold, 20),
        labelMedium = style(12, FontWeight.Medium, 18),
        labelSmall = style(11, FontWeight.Medium, 16),
    )

/** Long-form reading text (descriptions, messages): a touch smaller than body with looser leading. */
val Typography.paragraph: TextStyle
    get() = bodyLarge.copy(lineHeight = 1.5.em, lineBreak = LineBreak.Paragraph)

/** Small bold tracked caption for labels inside a card and eyebrows. Pass the text already in upper case. */
val Typography.overline: TextStyle
    get() = labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
