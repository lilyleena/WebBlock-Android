package com.kshrd.features.note.presentation.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kshrd.core.data.entity.DataState
import com.kshrd.core.presentation.OnEachEvent
import com.kshrd.coreui.components.feedback.EmptyStateView
import com.kshrd.coreui.components.screen.DataStateScreenShell
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import com.kshrd.features.R
import com.kshrd.features.note.presentation.components.NoteComposer
import com.kshrd.features.note.presentation.components.NoteListSkeleton
import com.kshrd.features.note.presentation.components.NoteRow
import com.kshrd.features.note.presentation.components.previewNote
import com.kshrd.features.note.presentation.model.NoteListEffect
import com.kshrd.features.note.presentation.model.NoteListEvent
import com.kshrd.features.note.presentation.model.NoteListUiState
import com.kshrd.features.note.presentation.viewmodel.NoteListViewModel

/**
 * The stateful entry point: gets the ViewModel, collects state, consumes effects, and hands
 * everything to the stateless [NoteListContent]. Navigation is a callback so this module never
 * depends on the app's graph.
 */
@Composable
fun NoteListScreen(
    onOpenNote: (Long) -> Unit,
    viewModel: NoteListViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    OnEachEvent(viewModel.effects) { effect ->
        when (effect) {
            is NoteListEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
            is NoteListEffect.OpenNote -> onOpenNote(effect.noteId)
        }
    }

    NoteListContent(state = state, snackbarHostState = snackbarHostState, onEvent = viewModel::onEvent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListContent(
    state: NoteListUiState,
    snackbarHostState: SnackbarHostState,
    onEvent: (NoteListEvent) -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.note_list_title)) }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            NoteComposer(
                title = state.draftTitle,
                body = state.draftBody,
                isSaving = state.isSaving,
                onTitleChange = { onEvent(NoteListEvent.DraftTitleChanged(it)) },
                onBodyChange = { onEvent(NoteListEvent.DraftBodyChanged(it)) },
                onSubmit = { onEvent(NoteListEvent.SubmitClicked) },
            )
            HorizontalDivider()
            DataStateScreenShell(
                state = state.notes,
                modifier = Modifier.fillMaxSize(),
                loading = { NoteListSkeleton() },
                error = {
                    val message = (state.notes as? DataState.Error)?.message
                    EmptyStateView(
                        title = stringResource(R.string.note_error_title),
                        message = message,
                        actionLabel = stringResource(R.string.action_retry),
                        onAction = { onEvent(NoteListEvent.RetryClicked) },
                    )
                },
                isEmpty = { it.isEmpty() },
                empty = { EmptyStateView(title = stringResource(R.string.note_empty_title)) },
                success = { notes ->
                    LazyColumn {
                        items(notes, key = { it.id }) { note ->
                            NoteRow(note = note, onClick = { onEvent(NoteListEvent.NoteClicked(note.id)) })
                            HorizontalDivider()
                        }
                    }
                },
            )
        }
    }
}

@ThemePreviews
@Composable
private fun NoteListContentPreview() {
    PreviewSurface(padding = 0) {
        NoteListContent(
            state =
                NoteListUiState(
                    notes = DataState.Success(listOf(previewNote, previewNote.copy(id = 2, title = "Second"))),
                    draftTitle = "Draft",
                ),
            snackbarHostState = SnackbarHostState(),
            onEvent = {},
        )
    }
}

@ThemePreviews
@Composable
private fun NoteListContentErrorPreview() {
    PreviewSurface(padding = 0) {
        NoteListContent(
            state = NoteListUiState(notes = DataState.Error(message = "No internet connection")),
            snackbarHostState = SnackbarHostState(),
            onEvent = {},
        )
    }
}
