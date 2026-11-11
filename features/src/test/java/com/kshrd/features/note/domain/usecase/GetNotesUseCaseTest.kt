package com.kshrd.features.note.domain.usecase

import com.kshrd.core.data.entity.DataState
import com.kshrd.features.note.domain.model.Note
import com.kshrd.features.note.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

/** Use cases are tested with a hand-written fake repository. No mocking library needed. */
class GetNotesUseCaseTest {
    private val note = Note(1, "Title", "Body", "2026-01-01T00:00:00")

    private class FakeNoteRepository(
        private val result: DataState<List<Note>>,
    ) : NoteRepository {
        override fun getNotes(): Flow<DataState<List<Note>>> = flowOf(result)

        override fun getNote(id: Long): Flow<DataState<Note>> = error("not used")

        override fun createNote(
            title: String,
            body: String,
        ): Flow<DataState<Note>> = error("not used")
    }

    @Test
    fun `emits Loading then Success`() =
        runTest {
            val useCase = GetNotesUseCase(FakeNoteRepository(DataState.Success(listOf(note))))

            val emissions = useCase().toList()

            assertEquals(listOf(DataState.Loading, DataState.Success(listOf(note))), emissions)
        }

    @Test
    fun `forwards repository errors`() =
        runTest {
            val useCase = GetNotesUseCase(FakeNoteRepository(DataState.Error(500, "boom")))

            val emissions = useCase().toList()

            assertEquals(DataState.Error(500, "boom"), emissions.last())
        }
}
