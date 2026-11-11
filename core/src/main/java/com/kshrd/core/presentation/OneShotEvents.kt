package com.kshrd.core.presentation

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * Buffered channel for one-shot UI effects (snackbar, navigation, dialogs).
 *
 * Never put one-shot effects in a `StateFlow`: a state is re-delivered on every recomposition and
 * configuration change, so a snackbar would show again. Each event here is delivered exactly once.
 */
class OneShotEvents<T> {
    private val channel = Channel<T>(Channel.BUFFERED)
    val events: Flow<T> = channel.receiveAsFlow()

    suspend fun emit(event: T) {
        channel.send(event)
    }

    fun tryEmit(event: T): Boolean = channel.trySend(event).isSuccess
}
