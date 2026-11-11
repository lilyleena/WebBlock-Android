package com.kshrd.features.note.domain.usecase

import com.kshrd.core.data.entity.DataState
import com.kshrd.core.data.entity.asUseCaseFlow
import com.kshrd.features.note.domain.model.Note
import com.kshrd.features.note.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNoteUseCase
    @Inject
    constructor(
        private val repository: NoteRepository,
    ) {
        operator fun invoke(id: Long): Flow<DataState<Note>> = repository.getNote(id).asUseCaseFlow()
    }
