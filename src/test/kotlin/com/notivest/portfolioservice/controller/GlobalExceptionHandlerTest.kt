package com.notivest.portfolioservice.controller

import com.notivest.portfolioservice.controller.exception.ConflictException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.springframework.http.HttpStatus
import org.springframework.web.context.request.WebRequest

class GlobalExceptionHandlerTest {
    private val handler = GlobalExceptionHandler()
    private val mockRequest: WebRequest =
        mock {
            on { getDescription(false) }.thenReturn("uri=/test")
        }

    @Test
    fun `should handle NoSuchElementException with 404`() {
        val exception = NoSuchElementException("Resource not found")

        val response = handler.handleNotFound(exception, mockRequest)

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertEquals(404, response.body?.status)
        assertEquals("Not Found", response.body?.error)
        assertEquals("Resource not found", response.body?.message)
        assertEquals("/test", response.body?.path)
    }

    @Test
    fun `should handle IllegalArgumentException with 400`() {
        val exception = IllegalArgumentException("Invalid parameter")

        val response = handler.handleBadRequest(exception, mockRequest)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals(400, response.body?.status)
        assertEquals("Bad Request", response.body?.error)
        assertEquals("Invalid parameter", response.body?.message)
        assertEquals("/test", response.body?.path)
    }

    @Test
    fun `should handle ConflictException with 409`() {
        val exception = ConflictException("Resource conflict")

        val response = handler.handleConflict(exception, mockRequest)

        assertEquals(HttpStatus.CONFLICT, response.statusCode)
        assertEquals(409, response.body?.status)
        assertEquals("Conflict", response.body?.error)
        assertEquals("Resource conflict", response.body?.message)
        assertEquals("/test", response.body?.path)
    }

    @Test
    fun `should handle general Exception with 500`() {
        val exception = RuntimeException("Unexpected error")

        val response = handler.handleRuntimeException(exception, mockRequest)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertEquals(500, response.body?.status)
        assertEquals("Internal Server Error", response.body?.error)
        assertEquals("Unexpected error", response.body?.message)
        assertEquals("/test", response.body?.path)
    }
}
