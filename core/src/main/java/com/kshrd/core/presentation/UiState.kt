package com.kshrd.core.presentation

import com.kshrd.core.data.entity.DataState
import com.kshrd.core.data.entity.mapSuccess

/**
 * Presentation-layer name for [DataState]. Use this in ViewModels and screens; nested variants
 * remain `DataState.Loading`, `DataState.Success`, and so on, because Kotlin typealiases do not
 * forward nested types.
 */
typealias UiState<R> = DataState<R>

fun <R, T> UiState<R>.mapSuccess(transform: (R) -> T): UiState<T> = mapSuccess(transform)
