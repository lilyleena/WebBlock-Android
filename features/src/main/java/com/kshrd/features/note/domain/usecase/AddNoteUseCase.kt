package com.kshrd.features.note.domain.usecase

import com.kshrd.core.data.entity.DataState
import com.kshrd.core.data.entity.asUseCaseFlow
import com.kshrd.features.note.domain.model.Note
import com.kshrd.features.note.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

/** Validation that belongs to the domain lives in the use case, not in the ViewModel or the UI. */
class AddNoteUseCase
    @Inject
    constructor(
        private val repository: NoteRepository,
    ) {
        operator fun invoke(
            title: String,
            body: String,
        ): Flow<DataState<Note>> {
            val trimmedTitle = title.trim()
            if (trimmedTitle.isEmpty()) {
                return flowOf(DataState.Error(message = "Title is required"))
            }
            return repository.createNote(trimmedTitle, body.trim()).asUseCaseFlow()
        }
    }
