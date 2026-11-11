package com.kshrd.features.note.domain.service

import com.kshrd.core.data.entity.DataState
import com.kshrd.features.note.domain.model.Note
import kotlinx.coroutines.flow.Flow

/**
 * Domain-facing contract of the remote source. The implementation lives in `data/service`
 * and is the only place that knows about Retrofit.
 */
interface NoteService {
    fun getNotes(): Flow<DataState<List<Note>>>

    fun getNote(id: Long): Flow<DataState<Note>>

    fun createNote(
        title: String,
        body: String,
    ): Flow<DataState<Note>>
}
