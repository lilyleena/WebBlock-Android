package com.kshrd.core.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow

/**
 * Collects a [OneShotEvents.events] flow while the screen is at least STARTED.
 * Use this in the screen composable to react to UiEffects.
 */
@Composable
fun <T> OnEachEvent(
    events: Flow<T>,
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
    block: suspend (T) -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(events, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(lifecycleState) {
            events.collect(block)
        }
    }
}
