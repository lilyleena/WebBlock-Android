package com.kshrd.coreui.components.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kshrd.core.data.entity.DataState
import com.kshrd.coreui.motion.AppMotion
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews

/**
 * Centralises the `when (state)` branching every screen needs. Pass one slot per phase; the
 * phases cross-fade. Provide [isEmpty] when a Success payload can mean "no results".
 *
 * ```kotlin
 * DataStateScreenShell(
 *     state = state.notes,
 *     loading = { NoteListSkeleton() },
 *     error = { ErrorView(onRetry) },
 *     isEmpty = { it.isEmpty() },
 *     empty = { EmptyStateView("No notes yet") },
 *     success = { notes -> NoteList(notes) },
 * )
 * ```
 */
@Composable
fun <T> DataStateScreenShell(
    state: DataState<T>,
    modifier: Modifier = Modifier,
    loading: @Composable BoxScope.() -> Unit,
    error: @Composable BoxScope.() -> Unit = loading,
    empty: @Composable BoxScope.() -> Unit = {},
    isEmpty: (T) -> Boolean = { false },
    success: @Composable BoxScope.(T) -> Unit,
    messageSuccess: @Composable BoxScope.(String) -> Unit = { empty() },
) {
    Box(modifier = modifier, propagateMinConstraints = true) {
        AnimatedContent(
            targetState = state,
            // Only a change of phase animates; new data in the same phase just recomposes.
            contentKey = { phaseOf(it, isEmpty) },
            transitionSpec = { (AppMotion.contentIn() togetherWith AppMotion.contentOut()).using(null) },
            label = "DataState",
        ) { target ->
            Box {
                when (target) {
                    is DataState.Loading -> loading()
                    is DataState.Error -> error()
                    is DataState.MessageSuccess -> messageSuccess(target.message)
                    is DataState.Success -> {
                        val data = target.data
                        if (isEmpty(data)) empty() else success(data)
                    }
                }
            }
        }
    }
}

private enum class Phase { Loading, Error, Message, Empty, Content }

private fun <T> phaseOf(
    state: DataState<T>,
    isEmpty: (T) -> Boolean,
): Phase =
    when (state) {
        is DataState.Loading -> Phase.Loading
        is DataState.Error -> Phase.Error
        is DataState.MessageSuccess -> Phase.Message
        is DataState.Success -> if (isEmpty(state.data)) Phase.Empty else Phase.Content
    }

@ThemePreviews
@Composable
private fun DataStateScreenShellSuccessPreview() {
    PreviewSurface {
        DataStateScreenShell(
            state = DataState.Success(listOf("Alice", "Bopha")),
            loading = { Text("Loading…") },
            isEmpty = { it.isEmpty() },
            empty = { Text("No records") },
            success = { names -> Text(names.joinToString()) },
        )
    }
}

@ThemePreviews
@Composable
private fun DataStateScreenShellEmptyPreview() {
    PreviewSurface {
        DataStateScreenShell(
            state = DataState.Success(emptyList<String>()),
            loading = { Text("Loading…") },
            isEmpty = { it.isEmpty() },
            empty = { Text("No records") },
            success = { names -> Text(names.joinToString()) },
        )
    }
}
