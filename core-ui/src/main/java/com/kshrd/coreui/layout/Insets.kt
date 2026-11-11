package com.kshrd.coreui.layout

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp

/** Gesture / navigation bar height plus [extra]: bottom content padding for a list that scrolls edge to edge. */
@Composable
fun navigationBarBottom(extra: Dp): Dp = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + extra
