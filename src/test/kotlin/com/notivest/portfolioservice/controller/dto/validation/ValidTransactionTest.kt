package com.notivest.portfolioservice.controller.dto.validation

import com.notivest.portfolioservice.controller.dto.PostTransactionRequest
import com.notivest.portfolioservice.models.enums.TransactionType
import jakarta.validation.ConstraintViolation
import jakarta.validation.Validation
import jakarta.validation.Validator
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

class ValidTransactionTest {
    private lateinit var validator: Validator

    @BeforeEach
    fun setUp() {
        val factory = Validation.buildDefaultValidatorFactory()
        validator = factory.validator
    }

    @Test
    fun `should validate BUY transaction with symbol and quantity`() {
        val request =
            PostTransactionRequest(
                accountId = UUID.randomUUID(),
                type = TransactionType.BUY,
                symbolId = "AAPL",
                qty = BigDecimal("10"),
                price = BigDecimal("150.0"),
                currency = "USD",
                tradeDate = LocalDate.now(),
            )

        val violations: Set<ConstraintViolation<PostTransactionRequest>> = validator.validate(request)
        assertTrue(violations.isEmpty())
    }

    @Test
    fun `should reject BUY transaction without symbol`() {
        val request =
            PostTransactionRequest(
                accountId = UUID.randomUUID(),
                type = TransactionType.BUY,
                symbolId = null,
                qty = BigDecimal("10"),
                price = BigDecimal("150.0"),
                currency = "USD",
                tradeDate = LocalDate.now(),
            )

        val violations: Set<ConstraintViolation<PostTransactionRequest>> = validator.validate(request)
        assertTrue(violations.isNotEmpty())
        assertTrue(violations.any { it.message.contains("Symbol ID is required") })
    }

    @Test
    fun `should reject BUY transaction without quantity`() {
        val request =
            PostTransactionRequest(
                accountId = UUID.randomUUID(),
                type = TransactionType.BUY,
                symbolId = "AAPL",
                qty = null,
                price = BigDecimal("150.0"),
                currency = "USD",
                tradeDate = LocalDate.now(),
            )

        val violations: Set<ConstraintViolation<PostTransactionRequest>> = validator.validate(request)
        assertTrue(violations.isNotEmpty())
        assertTrue(violations.any { it.message.contains("Quantity must be greater than 0") })
    }

    @Test
    fun `should reject BUY transaction with zero quantity`() {
        val request =
            PostTransactionRequest(
                accountId = UUID.randomUUID(),
                type = TransactionType.BUY,
                symbolId = "AAPL",
                qty = BigDecimal.ZERO,
                price = BigDecimal("150.0"),
                currency = "USD",
                tradeDate = LocalDate.now(),
            )

        val violations: Set<ConstraintViolation<PostTransactionRequest>> = validator.validate(request)
        assertTrue(violations.isNotEmpty())
        assertTrue(violations.any { it.message.contains("Quantity must be greater than 0") })
    }

    @Test
    fun `should validate DIVIDEND transaction without symbol and quantity requirements`() {
        val request =
            PostTransactionRequest(
                accountId = UUID.randomUUID(),
                type = TransactionType.DIVIDEND,
                symbolId = null,
                qty = null,
                price = null,
                currency = "USD",
                tradeDate = LocalDate.now(),
            )

        val violations: Set<ConstraintViolation<PostTransactionRequest>> = validator.validate(request)
        // Should only have basic validation errors, not custom transaction validation errors
        val customErrors =
            violations.filter {
                it.message.contains("Symbol ID is required") ||
                    it.message.contains("Quantity must be greater than 0")
            }
        assertTrue(customErrors.isEmpty())
    }
}
