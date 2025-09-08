package com.notivest.portfolioservice.controller.exception

/**
 * Excepción que representa un conflicto de recursos (HTTP 409)
 * Usado principalmente para violaciones de idempotencia
 */
class ConflictException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
