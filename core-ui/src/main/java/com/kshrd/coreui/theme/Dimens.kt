package com.kshrd.coreui.theme

import androidx.compose.ui.unit.dp

/**
 * Spacing scale (the iOS app's `Space`, on a 4dp grid). Use these instead of raw `dp` literals so
 * every screen shares the same rhythm:
 * - [xxxs] label over its value, title over its subtitle
 * - [xxs] icon next to its text, helper text under a field
 * - [xs] related elements (a section title and its card, a field label and its field)
 * - [s] rows inside a card, buttons side by side
 * - [m] between cards, and the padding inside a card
 * - [screen] horizontal page margin
 * - [l] / [xl] between unrelated groups
 */
object Space {
    val xxxs = 2.dp
    val xxs = 4.dp
    val xs = 8.dp
    val s = 12.dp
    val m = 16.dp

    /** Horizontal screen padding. */
    val screen = 18.dp
    val l = 24.dp
    val xl = 32.dp
}

/**
 * Corner radii (the iOS app's `Radius`), smallest to largest. Pick by what the surface is, not by eye:
 * - [small] tiny tiles and chips inside illustrations
 * - [tile] photos, icon tiles, rows nested inside a card
 * - [control] menu rows, banners, expandable rows
 * - [panel] panels nested in a page (multi-line fields, metric tiles, the photo drop zone)
 * - [card] every page-level card
 * - [sheet] bottom sheets and form cards
 * - [hero] the sign-in card
 */
object Radius {
    val small = 6.dp
    val tile = 10.dp // web --r-sm
    val control = 14.dp // web --r-md
    val panel = 14.dp
    val card = 20.dp // web --r-lg
    val sheet = 28.dp // Material bottom sheet
    val hero = 20.dp
}

/** Fixed component sizes shared across screens. */
object Sizes {
    /** Height of buttons and single-line fields. */
    val control = 50.dp

    /** Minimum height of compact controls (chips, the search field, toolbar buttons). */
    val compact = 44.dp

    /** Small icons beside text. */
    val iconSmall = 16.dp

    /** Icons inside rows and buttons. */
    val icon = 20.dp

    /** Toolbar icons. */
    val iconLarge = 24.dp
}
