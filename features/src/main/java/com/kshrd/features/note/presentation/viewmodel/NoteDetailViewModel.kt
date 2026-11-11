package com.kshrd.features.note.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.kshrd.core.data.entity.DataState
import com.kshrd.core.navigation.NoteDetail
import com.kshrd.core.presentation.UiState
import com.kshrd.features.note.domain.model.Note
import com.kshrd.features.note.domain.usecase.GetNoteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Reads its navigation argument from [SavedStateHandle] with the typed destination. */
@HiltViewModel
class NoteDetailViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val getNote: GetNoteUseCase,
    ) : ViewModel() {
        private val args = savedStateHandle.toRoute<NoteDetail>()

        private val _state = MutableStateFlow<UiState<Note>>(DataState.Loading)
        val state: StateFlow<UiState<Note>> = _state.asStateFlow()

        init {
            load()
        }

        fun load() {
            viewModelScope.launch {
                getNote(args.noteId).collect { _state.value = it }
            }
        }
    }
