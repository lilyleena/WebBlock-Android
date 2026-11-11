package com.kshrd.coreui.motion

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally

/**
 * The app's motion, Material 3 style. Screens move on a short shared horizontal axis (a 10% slide
 * plus a fade). Content swaps inside a screen (loading to loaded) cross-fade.
 */
@Suppress("MagicNumber") // Material 3 easing curves are defined by their control points
object AppMotion {
    val EmphasizedDecelerate: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
    val EmphasizedAccelerate: Easing = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)

    const val SCREEN_MS = 300
    const val SCREEN_EXIT_MS = 150
    const val CONTENT_IN_MS = 220
    const val CONTENT_OUT_MS = 90

    private const val SHIFT_DIVISOR = 10

    private fun shift(width: Int) = width / SHIFT_DIVISOR

    /** A screen opening on top. */
    fun forwardEnter(): EnterTransition =
        slideInHorizontally(tween(SCREEN_MS, easing = EmphasizedDecelerate)) { shift(it) } +
            fadeIn(tween(SCREEN_MS, easing = EmphasizedDecelerate))

    /** The screen underneath as another opens. */
    fun forwardExit(): ExitTransition =
        slideOutHorizontally(tween(SCREEN_MS, easing = EmphasizedDecelerate)) { -shift(it) } +
            fadeOut(tween(SCREEN_EXIT_MS, easing = EmphasizedAccelerate))

    /** The screen underneath coming back on Back. */
    fun backEnter(): EnterTransition =
        slideInHorizontally(tween(SCREEN_MS, easing = EmphasizedDecelerate)) { -shift(it) } +
            fadeIn(tween(SCREEN_MS, easing = EmphasizedDecelerate))

    /** The top screen leaving on Back. */
    fun backExit(): ExitTransition =
        slideOutHorizontally(tween(SCREEN_MS, easing = EmphasizedDecelerate)) { shift(it) } +
            fadeOut(tween(SCREEN_EXIT_MS, easing = EmphasizedAccelerate))

    /** Content swapping in place (a screen's loading, error and loaded states). */
    fun contentIn(): EnterTransition =
        fadeIn(tween(CONTENT_IN_MS, delayMillis = CONTENT_OUT_MS, easing = EmphasizedDecelerate))

    fun contentOut(): ExitTransition = fadeOut(tween(CONTENT_OUT_MS, easing = EmphasizedAccelerate))
}
