package com.kshrd.core.data.entity

import androidx.compose.runtime.Immutable

/**
 * Standard async state for anything loaded from a repository.
 *
 * Presentation code imports the alias [com.kshrd.core.presentation.UiState]; nested variants are
 * still accessed through [DataState] (for example `DataState.Loading`) because Kotlin typealiases
 * do not forward nested types.
 *
 * UI `when` expressions must handle all four branches. No `TODO()` in production branches.
 */
@Immutable
sealed interface DataState<out T> {
    @Immutable
    data object Loading : DataState<Nothing>

    @Immutable
    data class Success<T>(val data: T) : DataState<T>

    @Immutable
    data class Error(
        val code: Int? = null,
        val message: String? = null,
        val throwable: Throwable? = null,
    ) : DataState<Nothing>

    /** A call that succeeded but carried only a message (for example a delete or an update). */
    @Immutable
    data class MessageSuccess(val message: String) : DataState<Nothing>
}

fun <R, T> DataState<R>.mapSuccess(transform: (R) -> T): DataState<T> =
    when (this) {
        is DataState.Success -> DataState.Success(transform(data))
        is DataState.Error -> this
        is DataState.MessageSuccess -> this
        is DataState.Loading -> this
    }

/** Converts a [Result] from a data source into the standard async state. */
fun <T> Result<T>.toDataState(): DataState<T> =
    fold(
        onSuccess = { DataState.Success(it) },
        onFailure = { DataState.Error(message = it.message, throwable = it) },
    )
