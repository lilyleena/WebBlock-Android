package com.kshrd.features.note.data.api

import com.kshrd.core.base.BaseResponse
import com.kshrd.features.note.domain.model.CreateNoteRequest
import com.kshrd.features.note.domain.model.Note
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

private const val FAKE_LATENCY_MS = 600L
private const val HTTP_NOT_FOUND = 404

/**
 * A fake backend that implements [NoteApi] in memory so the sample runs without a server.
 *
 * It behaves like the real thing from the app's point of view: it is slow, it returns the same
 * [BaseResponse] envelope, and it produces an HTTP 404 for an unknown id. When a real backend
 * exists, delete this class and bind `retrofit.create(NoteApi::class.java)` in `FeatureApiModule`.
 * Nothing above the api layer changes.
 */
@Singleton
class InMemoryNoteApi
    @Inject
    constructor() : NoteApi {
        private val mutex = Mutex()
        private val notes =
            listOf(
                Triple("Read the architecture doc", "docs/ARCHITECTURE.md explains the module graph.", "2026-10-01T09:00:00"),
                Triple("Add a feature", "Follow docs/ADDING_A_FEATURE.md step by step.", "2026-10-02T09:30:00"),
                Triple("Run the quality gates", "./gradlew checkCode before every pull request.", "2026-10-03T10:15:00"),
            ).mapIndexed { index, (title, body, createdAt) -> Note(index + 1L, title, body, createdAt) }
                .toMutableList()

        override suspend fun getNotes(): Response<BaseResponse<List<Note>>> {
            delay(FAKE_LATENCY_MS)
            val snapshot = mutex.withLock { notes.toList() }
            return Response.success(BaseResponse(status = "OK", message = "Fetched", data = snapshot))
        }

        override suspend fun getNote(id: Long): Response<BaseResponse<Note>> {
            delay(FAKE_LATENCY_MS)
            val note = mutex.withLock { notes.firstOrNull { it.id == id } }
            return if (note != null) {
                Response.success(BaseResponse(status = "OK", message = "Fetched", data = note))
            } else {
                Response.error(HTTP_NOT_FOUND, """{"message":"Note $id was not found"}""".toResponseBody())
            }
        }

        override suspend fun createNote(request: CreateNoteRequest): Response<BaseResponse<Note>> {
            delay(FAKE_LATENCY_MS)
            val created =
                mutex.withLock {
                    val next = (notes.maxOfOrNull { it.id } ?: 0L) + 1
                    Note(next, request.title, request.body, LocalDateTime.now().withNano(0).toString()).also { notes += it }
                }
            return Response.success(BaseResponse(status = "CREATED", message = "Note created", data = created))
        }
    }
