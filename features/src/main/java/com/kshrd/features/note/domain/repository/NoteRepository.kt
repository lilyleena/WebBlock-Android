package com.kshrd.features.note.domain.repository

import com.kshrd.core.data.entity.DataState
import com.kshrd.features.note.domain.model.Note
import kotlinx.coroutines.flow.Flow

/**
 * What the domain can ask for. Use cases depend on this interface, never on a service or API.
 * A repository is where caching, offline fallback, or combining several services would go.
 */
interface NoteRepository {
    fun getNotes(): Flow<DataState<List<Note>>>

    fun getNote(id: Long): Flow<DataState<Note>>

    fun createNote(
        title: String,
        body: String,
    ): Flow<DataState<Note>>
}
