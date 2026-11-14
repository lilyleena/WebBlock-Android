package com.kshrd.core.api

import kotlinx.serialization.Serializable
import retrofit2.HttpException

/** RFC 9457 problem details, the union of the five services' shapes (docs/API-MAPPING.md section 1). */
@Serializable
data class ProblemDto(
    val type: String? = null,
    val title: String? = null,
    val status: Int? = null,
    val detail: String? = null,
    /** Project and Provisioning send an UPPER_SNAKE code as well. */
    val code: String? = null,
    val requestId: String? = null,
    /** User and Notification. */
    val fieldErrors: Map<String, List<String>>? = null,
    /** Project and Provisioning. */
    val errors: List<FieldErrorDto>? = null,
)

@Serializable
data class FieldErrorDto(
    val field: String? = null,
    val code: String? = null,
)

/**
 * A v2 answer that is not 2xx.
 *
 * [code] is always kebab-case: the end of `type` (`urn:webblock:problem:<code>`), else Project's UPPER_SNAKE `code`
 * converted (`STORAGE_LIMIT_REACHED` → `storage-limit-reached`), else `http-<status>`.
 */
class ApiException(
    val status: Int,
    val code: String,
    val title: String?,
    val detail: String?,
    val requestId: String?,
    /** Seconds from the `Retry-After` header (only `otp-resend-too-soon` sends it). */
    val retryAfterSeconds: Int? = null,
    /** Field name to the server's messages or codes, from either field-error shape. */
    val fieldErrors: Map<String, List<String>> = emptyMap(),
) : Exception("HTTP $status $code") {
    companion object {
        private const val TYPE_PREFIX = "urn:webblock:problem:"

        fun from(error: HttpException): ApiException {
            val response = error.response()
            return parse(
                status = error.code(),
                body = response?.errorBody()?.string(),
                retryAfter = response?.headers()?.get("Retry-After"),
                requestIdHeader = response?.headers()?.get("X-Request-Id"),
            )
        }

        fun parse(
            status: Int,
            body: String?,
            retryAfter: String? = null,
            requestIdHeader: String? = null,
        ): ApiException {
            // A body that is not a problem (an HTML error page from the edge, for example) leaves only the status.
            val problem =
                body?.takeIf { it.isNotBlank() }?.let {
                    runCatching { ApiJson.decodeFromString(ProblemDto.serializer(), it) }.getOrNull()
                }
            val fields =
                problem?.fieldErrors
                    ?: problem?.errors.orEmpty().filter { it.field != null }.groupBy({ it.field.orEmpty() }, { it.code.orEmpty() })
            return ApiException(
                status = status,
                code = codeOf(problem) ?: "http-$status",
                title = problem?.title,
                detail = problem?.detail,
                requestId = requestIdHeader ?: problem?.requestId,
                retryAfterSeconds = retryAfter?.trim()?.toIntOrNull(),
                fieldErrors = fields,
            )
        }

        private fun codeOf(problem: ProblemDto?): String? {
            val fromType = problem?.type?.takeIf { it.startsWith(TYPE_PREFIX) }?.removePrefix(TYPE_PREFIX)
            val fromCode = problem?.code?.lowercase()?.replace('_', '-')
            return (fromType ?: fromCode)?.takeIf { it.isNotBlank() }
        }
    }
}
