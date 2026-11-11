package com.kshrd.features.note.data.service

import com.kshrd.core.base.BaseResponse
import com.kshrd.core.data.entity.DataState
import com.kshrd.core.network.RemoteDataSource
import com.kshrd.features.note.data.api.NoteApi
import com.kshrd.features.note.domain.model.CreateNoteRequest
import com.kshrd.features.note.domain.model.Note
import com.kshrd.features.note.domain.service.NoteService
import kotlinx.coroutines.flow.Flow
import retrofit2.Response
import javax.inject.Inject

/** Wraps each [NoteApi] call in [RemoteDataSource] so errors become [DataState] in one place. */
class NoteServiceImpl
    @Inject
    constructor(
        private val noteApi: NoteApi,
    ) : NoteService {
        override fun getNotes(): Flow<DataState<List<Note>>> =
            object : RemoteDataSource<List<Note>>() {
                override suspend fun createCall(): Response<BaseResponse<List<Note>>> = noteApi.getNotes()
            }.asFlow()

        override fun getNote(id: Long): Flow<DataState<Note>> =
            object : RemoteDataSource<Note>() {
                override suspend fun createCall(): Response<BaseResponse<Note>> = noteApi.getNote(id)
            }.asFlow()

        override fun createNote(
            title: String,
            body: String,
        ): Flow<DataState<Note>> =
            object : RemoteDataSource<Note>() {
                override suspend fun createCall(): Response<BaseResponse<Note>> =
                    noteApi.createNote(CreateNoteRequest(title = title, body = body))
            }.asFlow()
    }
