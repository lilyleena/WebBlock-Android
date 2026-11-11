package com.kshrd.core.network

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import com.kshrd.core.base.BaseResponse
import com.kshrd.core.data.entity.DataState
import com.kshrd.core.data.entity.ErrorBaseResponse
import com.kshrd.core.data.entity.ErrorCode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import retrofit2.Response
import java.net.ConnectException
import java.net.SocketException
import java.net.UnknownHostException
import java.util.concurrent.CancellationException

private const val TAG = "RemoteDataSource"
private const val HTTP_UNAUTHORIZED = 401
private const val HTTP_FORBIDDEN = 403
private const val HTTP_NOT_FOUND = 404
private const val HTTP_SERVER_ERROR_MIN = 500
private const val HTTP_SERVER_ERROR_MAX = 599

/**
 * Turns one Retrofit call into a `Flow<DataState<T>>`.
 *
 * Every `*ServiceImpl` wraps its API call in this class so HTTP errors, missing bodies and
 * connection failures are mapped to [DataState] in exactly one place.
 *
 * ```kotlin
 * override fun getNotes(): Flow<DataState<List<Note>>> =
 *     object : RemoteDataSource<List<Note>>() {
 *         override suspend fun createCall() = noteApi.getNotes()
 *     }.asFlow()
 * ```
 */
abstract class RemoteDataSource<T> {
    protected abstract suspend fun createCall(): Response<BaseResponse<T>>

    fun asFlow(): Flow<DataState<T>> =
        flow {
            emit(DataState.Loading)
            val response = createCall()

            if (response.isSuccessful) {
                val body = response.body()?.data
                if (body != null) {
                    emit(DataState.Success(body))
                } else {
                    // 2xx with no payload: the backend only had a message for us.
                    emit(DataState.MessageSuccess(response.body()?.message ?: "Success"))
                }
            } else {
                val errorBodyString = response.errorBody()?.string()
                val errorResponse = parseErrorBody(errorBodyString)
                val message =
                    resolveHttpErrorMessage(
                        errorResponse = errorResponse,
                        errorBodyString = errorBodyString,
                        httpCode = response.code(),
                        responseMessage = response.message(),
                    )
                val code = errorResponse?.errors?.code ?: response.code()
                emit(DataState.Error(code, message))
            }
        }.catch { e ->
            when (e) {
                is CancellationException -> throw e
                is UnknownHostException, is ConnectException, is SocketException ->
                    emit(DataState.Error(ErrorCode.CONNECTION_ERROR, "No internet connection"))
                else -> emit(DataState.Error(ErrorCode.UNKNOWN_ERROR, e.message ?: "Unexpected error"))
            }
        }
}

private fun parseErrorBody(errorBodyString: String?): ErrorBaseResponse? =
    try {
        Gson().fromJson(errorBodyString, ErrorBaseResponse::class.java)
    } catch (e: JsonSyntaxException) {
        Log.e(TAG, "Failed to parse error response: ${e.message}")
        null
    }

/** The `message` field of a JSON error body, when the body is JSON and has one. */
private fun rootMessageOf(errorBodyString: String?): String? =
    try {
        val obj = Gson().fromJson(errorBodyString, JsonObject::class.java)
        val m = obj?.get("message")
        if (m != null && m.isJsonPrimitive) m.asString.trim().takeIf { it.isNotEmpty() } else null
    } catch (e: JsonSyntaxException) {
        Log.d(TAG, "Error body is not JSON: ${e.message}")
        null
    }

private fun defaultMessageFor(httpCode: Int): String =
    when (httpCode) {
        HTTP_UNAUTHORIZED -> "Please sign in again."
        HTTP_FORBIDDEN -> "You don't have permission to do this."
        HTTP_NOT_FOUND -> "Not found"
        in HTTP_SERVER_ERROR_MIN..HTTP_SERVER_ERROR_MAX -> "Server error. Please try again later."
        else -> "Something went wrong. Please try again."
    }

/** Picks the most specific message available: nested error, root message, raw text, HTTP reason, default. */
private fun resolveHttpErrorMessage(
    errorResponse: ErrorBaseResponse?,
    errorBodyString: String?,
    httpCode: Int,
    responseMessage: String?,
): String {
    val nested = errorResponse?.errors?.errorMessage?.trim()?.takeIf { it.isNotEmpty() }
    val raw = errorBodyString?.trim().orEmpty().takeIf { it.isNotEmpty() && !it.startsWith("{") }
    val reason = responseMessage?.trim()?.takeIf { it.isNotEmpty() }
    return nested
        ?: rootMessageOf(errorBodyString)
        ?: raw
        ?: reason
        ?: defaultMessageFor(httpCode)
}
