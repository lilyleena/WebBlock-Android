package com.kshrd.features.remote.repository

import com.kshrd.core.api.apiCall
import com.kshrd.core.api.toErrorState
import com.kshrd.core.data.entity.DataState
import java.util.concurrent.CancellationException

/** Runs [block]; any failure but a cancellation becomes a failed [Result]. */
internal suspend fun <T> attempt(block: suspend () -> T): Result<T> =
    runCatching { block() }.onFailure { if (it is CancellationException) throw it }

/** One load as a [DataState]: Success, or the Error the screen shows (never thrown, except a cancellation). */
internal suspend fun <T> loadState(block: suspend () -> T): DataState<T> =
    attempt { apiCall(block) }.fold(onSuccess = { DataState.Success(it) }, onFailure = { it.toErrorState() })

/** A part of a screen that may fail on its own (a service down, a plan rule): `null` instead of an error. */
internal suspend fun <T> optional(block: suspend () -> T): T? = attempt(block).getOrNull()
