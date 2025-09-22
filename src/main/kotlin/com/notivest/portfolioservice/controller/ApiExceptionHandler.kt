package com.notivest.portfolioservice.controller

import com.notivest.portfolioservice.exception.InvalidUserIdException
import com.notivest.portfolioservice.exception.NotFoundException
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.time.Instant

@ControllerAdvice
class ApiExceptionHandler {

    data class ErrorBody(
        val timestamp: String = Instant.now().toString(),
        val status: Int,
        val error: String,
        val message: String?,
        val path: String
    )

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFound(ex: NotFoundException, req: HttpServletRequest): ResponseEntity<ErrorBody> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ErrorBody(
                status = 404,
                error = "Not Found",
                message = ex.message,
                path = req.requestURI
            )
        )

    @ExceptionHandler(InvalidUserIdException::class)
    fun handleInvalidUser(ex: InvalidUserIdException, req: HttpServletRequest): ResponseEntity<ErrorBody> =
        ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            ErrorBody(
                status = 401,
                error = "Unauthorized",
                message = ex.message,
                path = req.requestURI
            )
        )

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleBeanValidation(ex: MethodArgumentNotValidException, req: HttpServletRequest): ResponseEntity<Any> {
        val fieldErrors = ex.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "invalid") }
        return ResponseEntity.badRequest().body(
            mapOf(
                "timestamp" to Instant.now().toString(),
                "status" to 400,
                "error" to "Bad Request",
                "message" to "Validation failed",
                "path" to req.requestURI,
                "fields" to fieldErrors
            )
        )
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolations(ex: ConstraintViolationException, req: HttpServletRequest): ResponseEntity<ErrorBody> =
        ResponseEntity.badRequest().body(
            ErrorBody(
                status = 400,
                error = "Bad Request",
                message = ex.message,
                path = req.requestURI
            )
        )
}