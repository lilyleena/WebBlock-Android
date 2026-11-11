package com.kshrd.features.note.data.repository

import com.kshrd.core.data.entity.DataState
import com.kshrd.features.note.domain.model.Note
import com.kshrd.features.note.domain.repository.NoteRepository
import com.kshrd.features.note.domain.service.NoteService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Delegates to the remote service. Add a local cache here when the feature needs one. */
class NoteRepositoryImpl
    @Inject
    constructor(
        private val noteService: NoteService,
    ) : NoteRepository {
        override fun getNotes(): Flow<DataState<List<Note>>> = noteService.getNotes()

        override fun getNote(id: Long): Flow<DataState<Note>> = noteService.getNote(id)

        override fun createNote(
            title: String,
            body: String,
        ): Flow<DataState<Note>> = noteService.createNote(title, body)
    }
