package com.kshrd.features.note.presentation.model

import androidx.compose.runtime.Immutable
import com.kshrd.core.data.entity.DataState
import com.kshrd.core.presentation.UiState
import com.kshrd.features.note.domain.model.Note

/**
 * The screen contract: one immutable state, the events the user can send, and the one-shot
 * effects the ViewModel can fire. Every screen gets a file like this.
 */
@Immutable
data class NoteListUiState(
    val notes: UiState<List<Note>> = DataState.Loading,
    val draftTitle: String = "",
    val draftBody: String = "",
    val isSaving: Boolean = false,
)

/** What the user did. The ViewModel handles all of them in `onEvent`. */
sealed interface NoteListEvent {
    data class DraftTitleChanged(val value: String) : NoteListEvent

    data class DraftBodyChanged(val value: String) : NoteListEvent

    data object SubmitClicked : NoteListEvent

    data object RetryClicked : NoteListEvent

    data class NoteClicked(val noteId: Long) : NoteListEvent
}

/** Things that happen once: a snackbar, a navigation. Never stored in state. */
sealed interface NoteListEffect {
    data class ShowMessage(val message: String) : NoteListEffect

    data class OpenNote(val noteId: Long) : NoteListEffect
}
