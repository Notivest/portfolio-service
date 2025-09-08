package com.notivest.portfolioservice.controller

import com.notivest.portfolioservice.controller.exception.ConflictException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import java.time.OffsetDateTime

data class ErrorResponse(
    val timestamp: OffsetDateTime = OffsetDateTime.now(),
    val status: Int,
    val error: String,
    val message: String,
    val path: String? = null,
)

@ControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFound(
        ex: NoSuchElementException,
        request: WebRequest,
    ): ResponseEntity<ErrorResponse> {
        val error =
            ErrorResponse(
                status = 404,
                error = "Not Found",
                message = ex.message ?: "Resource not found",
                path = request.getDescription(false).removePrefix("uri="),
            )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleBadRequest(
        ex: IllegalArgumentException,
        request: WebRequest,
    ): ResponseEntity<ErrorResponse> {
        val error =
            ErrorResponse(
                status = 400,
                error = "Bad Request",
                message = ex.message ?: "Invalid request parameters",
                path = request.getDescription(false).removePrefix("uri="),
            )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error)
    }

    @ExceptionHandler(ConflictException::class)
    fun handleConflict(
        ex: ConflictException,
        request: WebRequest,
    ): ResponseEntity<ErrorResponse> {
        val error =
            ErrorResponse(
                status = 409,
                error = "Conflict",
                message = ex.message ?: "Resource conflict",
                path = request.getDescription(false).removePrefix("uri="),
            )
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationErrors(
        ex: MethodArgumentNotValidException,
        request: WebRequest,
    ): ResponseEntity<ErrorResponse> {
        val errors = ex.bindingResult.fieldErrors.map { "${it.field}: ${it.defaultMessage}" }
        val error =
            ErrorResponse(
                status = 400,
                error = "Validation Failed",
                message = "Invalid input: ${errors.joinToString(", ")}",
                path = request.getDescription(false).removePrefix("uri="),
            )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error)
    }

    @ExceptionHandler(RuntimeException::class)
    fun handleRuntimeException(
        ex: RuntimeException,
        request: WebRequest,
    ): ResponseEntity<ErrorResponse> {
        val error =
            ErrorResponse(
                status = 500,
                error = "Internal Server Error",
                message = ex.message ?: "An unexpected error occurred",
                path = request.getDescription(false).removePrefix("uri="),
            )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error)
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneral(
        ex: Exception,
        request: WebRequest,
    ): ResponseEntity<ErrorResponse> {
        val error =
            ErrorResponse(
                status = 500,
                error = "Internal Server Error",
                message = "An unexpected error occurred",
                path = request.getDescription(false).removePrefix("uri="),
            )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error)
    }
}
