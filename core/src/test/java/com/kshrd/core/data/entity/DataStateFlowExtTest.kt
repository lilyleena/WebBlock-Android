package com.kshrd.core.data.entity

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class DataStateFlowExtTest {
    @Test
    fun `emits Loading first then forwards Success`() =
        runTest {
            val result = flowOf<DataState<Int>>(DataState.Success(7)).asUseCaseFlow().toList()

            assertEquals(listOf(DataState.Loading, DataState.Success(7)), result)
        }

    @Test
    fun `normalizes a bare Error`() =
        runTest {
            val result = flowOf<DataState<Int>>(DataState.Error()).asUseCaseFlow().toList()

            assertEquals(DataState.Error(-1, "Unknown error occurred"), result.last())
        }

    @Test
    fun `drops repository Loading and MessageSuccess`() =
        runTest {
            val result =
                flowOf<DataState<Int>>(DataState.Loading, DataState.MessageSuccess("ok"), DataState.Success(1))
                    .asUseCaseFlow()
                    .toList()

            assertEquals(listOf(DataState.Loading, DataState.Success(1)), result)
        }
}
