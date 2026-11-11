package com.kshrd.features.note.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kshrd.core.data.entity.DataState
import com.kshrd.core.presentation.OneShotEvents
import com.kshrd.features.note.domain.usecase.AddNoteUseCase
import com.kshrd.features.note.domain.usecase.GetNotesUseCase
import com.kshrd.features.note.presentation.model.NoteListEffect
import com.kshrd.features.note.presentation.model.NoteListEvent
import com.kshrd.features.note.presentation.model.NoteListUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Rules this ViewModel follows (and yours should too):
 * - exposes one immutable [StateFlow] of [NoteListUiState];
 * - takes every user action through [onEvent];
 * - sends one-shot effects through [OneShotEvents], never through state;
 * - calls use cases only, never a repository, service or API directly;
 * - has no Android `Context`.
 */
@HiltViewModel
class NoteListViewModel
    @Inject
    constructor(
        private val getNotes: GetNotesUseCase,
        private val addNote: AddNoteUseCase,
    ) : ViewModel() {
        private val _state = MutableStateFlow(NoteListUiState())
        val state: StateFlow<NoteListUiState> = _state.asStateFlow()

        private val _effects = OneShotEvents<NoteListEffect>()
        val effects = _effects.events

        init {
            loadNotes()
        }

        fun onEvent(event: NoteListEvent) {
            when (event) {
                is NoteListEvent.DraftTitleChanged -> _state.update { it.copy(draftTitle = event.value) }
                is NoteListEvent.DraftBodyChanged -> _state.update { it.copy(draftBody = event.value) }
                NoteListEvent.SubmitClicked -> submit()
                NoteListEvent.RetryClicked -> loadNotes()
                is NoteListEvent.NoteClicked -> _effects.tryEmit(NoteListEffect.OpenNote(event.noteId))
            }
        }

        private fun loadNotes() {
            viewModelScope.launch {
                getNotes().collect { result -> _state.update { it.copy(notes = result) } }
            }
        }

        private fun submit() {
            val current = _state.value
            if (current.isSaving) return
            viewModelScope.launch {
                addNote(current.draftTitle, current.draftBody).collect { result ->
                    when (result) {
                        is DataState.Loading -> _state.update { it.copy(isSaving = true) }
                        is DataState.Success -> {
                            _state.update { it.copy(isSaving = false, draftTitle = "", draftBody = "") }
                            _effects.emit(NoteListEffect.ShowMessage("Saved \"${result.data.title}\""))
                            loadNotes()
                        }
                        is DataState.Error -> {
                            _state.update { it.copy(isSaving = false) }
                            _effects.emit(NoteListEffect.ShowMessage(result.message ?: "Could not save"))
                        }
                        is DataState.MessageSuccess -> {
                            _state.update { it.copy(isSaving = false) }
                            _effects.emit(NoteListEffect.ShowMessage(result.message))
                        }
                    }
                }
            }
        }
    }
