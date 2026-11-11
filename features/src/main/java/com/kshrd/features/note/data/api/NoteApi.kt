package com.kshrd.features.note.data.api

import com.kshrd.core.base.BaseResponse
import com.kshrd.features.note.domain.model.CreateNoteRequest
import com.kshrd.features.note.domain.model.Note
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Retrofit definition of the notes endpoints. One interface per resource.
 * Every call returns `Response<BaseResponse<T>>` so `RemoteDataSource` can map it.
 */
interface NoteApi {
    @GET("notes")
    suspend fun getNotes(): Response<BaseResponse<List<Note>>>

    @GET("notes/{id}")
    suspend fun getNote(
        @Path("id") id: Long,
    ): Response<BaseResponse<Note>>

    @POST("notes")
    suspend fun createNote(
        @Body request: CreateNoteRequest,
    ): Response<BaseResponse<Note>>
}
