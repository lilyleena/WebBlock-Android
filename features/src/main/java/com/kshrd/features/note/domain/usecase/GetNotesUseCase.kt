package com.kshrd.features.note.domain.usecase

import com.kshrd.core.data.entity.DataState
import com.kshrd.core.data.entity.asUseCaseFlow
import com.kshrd.features.note.domain.model.Note
import com.kshrd.features.note.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * One use case = one user intention. It has a single `invoke` and no Android imports, so it is
 * trivially unit-testable with a fake repository (see `GetNotesUseCaseTest`).
 *
 * `@Inject constructor` is enough for Hilt; no `@Provides` needed.
 */
class GetNotesUseCase
    @Inject
    constructor(
        private val repository: NoteRepository,
    ) {
        operator fun invoke(): Flow<DataState<List<Note>>> =
            repository.getNotes().asUseCaseFlow()
    }
