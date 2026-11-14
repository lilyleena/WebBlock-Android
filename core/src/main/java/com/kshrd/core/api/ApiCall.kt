package com.kshrd.core.api

import com.kshrd.core.data.entity.DataState
import com.kshrd.core.data.entity.ErrorCode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import java.util.concurrent.CancellationException

/** Runs one v2 call; a non-2xx answer becomes an [ApiException] with the problem's code. */
suspend fun <T> apiCall(block: suspend () -> T): T =
    try {
        block()
    } catch (e: HttpException) {
        throw ApiException.from(e)
    }

/**
 * One v2 call as the app's standard `Flow<DataState<T>>`: Success, or Error with the HTTP status as `code`
 * ([ErrorCode.CONNECTION_ERROR] when there is no answer) and a sentence the owner can act on as `message`.
 */
fun <T> apiFlow(block: suspend () -> T): Flow<DataState<T>> =
    flow<DataState<T>> { emit(DataState.Success(apiCall(block))) }
        .catch { e ->
            if (e is CancellationException) throw e
            emit(e.toErrorState())
        }

/** The [DataState.Error] for a failed v2 call. */
fun Throwable.toErrorState(): DataState.Error =
    when (this) {
        is ApiException -> DataState.Error(status, ApiMessages.forProblem(this), this)
        is HttpException -> ApiException.from(this).toErrorState()
        is SerializationException -> DataState.Error(ErrorCode.UNKNOWN_ERROR, ApiMessages.UNEXPECTED_ANSWER, this)
        is IOException -> DataState.Error(ErrorCode.CONNECTION_ERROR, ApiMessages.NO_CONNECTION, this)
        else -> DataState.Error(ErrorCode.UNKNOWN_ERROR, message ?: ApiMessages.GENERIC, this)
    }
