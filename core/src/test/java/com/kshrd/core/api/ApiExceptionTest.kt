package com.kshrd.core.api

import com.kshrd.core.data.entity.DataState
import com.kshrd.core.data.entity.ErrorCode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.net.UnknownHostException

class ApiExceptionTest {
    @Test
    fun `the code is the end of the problem type`() {
        val error =
            ApiException.parse(
                401,
                """{"type":"urn:webblock:problem:invalid-credentials","title":"Unauthorized","status":401,"requestId":"r1"}""",
            )

        assertEquals("invalid-credentials", error.code)
        assertEquals("r1", error.requestId)
        assertEquals("The email or password is wrong.", ApiMessages.forProblem(error))
    }

    @Test
    fun `Project's UPPER_SNAKE code becomes kebab case`() {
        val error = ApiException.parse(422, """{"title":"Unprocessable","status":422,"code":"STORAGE_LIMIT_REACHED"}""")

        assertEquals("storage-limit-reached", error.code)
    }

    @Test
    fun `Retry-After is read for otp-resend-too-soon`() {
        val error = ApiException.parse(429, """{"type":"urn:webblock:problem:otp-resend-too-soon","status":429}""", retryAfter = " 42 ")

        assertEquals(42, error.retryAfterSeconds)
        assertEquals("Wait 42 seconds before asking for a new code.", ApiMessages.forProblem(error))
    }

    @Test
    fun `without Retry-After the wait is a minute`() {
        val error = ApiException.parse(429, """{"type":"urn:webblock:problem:otp-resend-too-soon","status":429}""")

        assertNull(error.retryAfterSeconds)
        assertEquals("Wait a minute before asking for a new code.", ApiMessages.forProblem(error))
    }

    @Test
    fun `the request id header wins over the body`() {
        val error = ApiException.parse(500, """{"status":500,"requestId":"body"}""", requestIdHeader = "header")

        assertEquals("header", error.requestId)
    }

    @Test
    fun `a body that is not a problem leaves the status`() {
        val error = ApiException.parse(502, "<html>Bad gateway</html>")

        assertEquals("http-502", error.code)
        assertEquals("Server error. Please try again later.", ApiMessages.forProblem(error))
    }

    @Test
    fun `an unknown code shows the server's detail`() {
        val error = ApiException.parse(422, """{"type":"urn:webblock:problem:something-new","detail":"Explained by the server."}""")

        assertEquals("Explained by the server.", ApiMessages.forProblem(error))
    }

    @Test
    fun `both field error shapes are read`() {
        val user = ApiException.parse(400, """{"status":400,"fieldErrors":{"email":["must be an email"]}}""")
        val project = ApiException.parse(400, """{"status":400,"errors":[{"field":"name","code":"REQUIRED"}]}""")

        assertEquals(mapOf("email" to listOf("must be an email")), user.fieldErrors)
        assertEquals(mapOf("name" to listOf("REQUIRED")), project.fieldErrors)
    }

    @Test
    fun `no answer is a connection error`() {
        val state = UnknownHostException("wb-api.roa-dev.digital").toErrorState()

        assertEquals(DataState.Error(ErrorCode.CONNECTION_ERROR, ApiMessages.NO_CONNECTION, state.throwable), state)
    }
}
