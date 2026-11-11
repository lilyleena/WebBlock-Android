package com.kshrd.coreui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Shape scale tuned to the iOS app; every value comes from [Radius].
 * - small 8dp: tiny tiles
 * - medium 14dp: controls (`Radius.control`)
 * - large 24dp: cards (`Radius.card`)
 * - extraLarge 32dp: the sign-in card
 */
val AppShapes =
    Shapes(
        extraSmall = RoundedCornerShape(4.dp),
        small = RoundedCornerShape(Radius.small),
        medium = RoundedCornerShape(Radius.control),
        large = RoundedCornerShape(Radius.card),
        extraLarge = RoundedCornerShape(Radius.hero),
    )

/** Fully rounded ends for buttons, fields and chips. */
val PillShape = RoundedCornerShape(percent = 50)

/** Page-level cards. */
val CardShape = RoundedCornerShape(Radius.card)

/** Bottom sheets: rounded top corners only. */
val SheetShape = RoundedCornerShape(topStart = Radius.sheet, topEnd = Radius.sheet)

/** Text fields and dropdowns (web dashboard inputs: rounded rectangle, not a capsule). */
val FieldShape = RoundedCornerShape(Radius.control)
