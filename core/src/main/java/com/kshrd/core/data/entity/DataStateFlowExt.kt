package com.kshrd.core.data.entity

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Wraps a repository [DataState] flow in the standard use-case shape:
 * emits [DataState.Loading] first, forwards Success, and normalizes Error
 * (null code becomes -1, null message becomes "Unknown error occurred").
 * Repository-side Loading and MessageSuccess emissions are dropped.
 */
fun <T> Flow<DataState<T>>.asUseCaseFlow(): Flow<DataState<T>> =
    flow {
        emit(DataState.Loading)
        collect { result ->
            when (result) {
                is DataState.Success -> emit(DataState.Success(result.data))
                is DataState.Error ->
                    emit(
                        DataState.Error(result.code ?: -1, result.message ?: "Unknown error occurred"),
                    )
                else -> Unit
            }
        }
    }
