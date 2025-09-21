package com.notivest.portfolioservice.dto

import com.notivest.portfolioservice.dto.holding.request.HoldingCreateRequest
import com.notivest.portfolioservice.dto.holding.request.HoldingUpdateRequest
import jakarta.validation.Validation
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class HoldingRequestDtoTest {

    companion object {
        private val factory = Validation.buildDefaultValidatorFactory()
        private val validator = factory.validator

        @JvmStatic @BeforeAll
        fun beforeAll() { /* no-op */ }
        @JvmStatic @AfterAll
        fun afterAll() { factory.close() }
    }

    @Test
    fun `HoldingCreateRequest valid create with position`() {
        val dto = HoldingCreateRequest(
            symbol = "BRK.B",
            quantity = BigDecimal("10.50"),
            avgCost = BigDecimal("350.00"),
            note = "Long-term"
        )
        val violations = validator.validate(dto)
        assertThat(violations).isEmpty()
    }

    @Test
    fun `HoldingCreateRequest valid create as watchlist (no quantity nor avgCost)`() {
        val dto = HoldingCreateRequest(symbol = "RDS-A", note = "Watch only")
        val violations = validator.validate(dto)
        assertThat(violations).isEmpty()
    }

    @Test
    fun `HoldingCreateRequest invalid symbol pattern - lowercase`() {
        val dto = HoldingCreateRequest(symbol = "aapl")
        val violations = validator.validate(dto)
        assertThat(violations).anyMatch { it.propertyPath.toString() == "symbol" }
    }

    @Test
    fun `HoldingCreateRequest invalid symbol pattern - too long`() {
        val dto = HoldingCreateRequest(symbol = "THIS-IS-TOO-LONG")
        val violations = validator.validate(dto)
        assertThat(violations).anyMatch { it.propertyPath.toString() == "symbol" }
    }

    @Test
    fun `HoldingCreateRequest invalid symbol pattern - underscore not allowed`() {
        val dto = HoldingCreateRequest(symbol = "ABC_DEF")
        val violations = validator.validate(dto)
        assertThat(violations).anyMatch { it.propertyPath.toString() == "symbol" }
    }

    @Test
    fun `HoldingCreateRequest invalid negative quantity`() {
        val dto = HoldingCreateRequest(symbol = "AAPL", quantity = BigDecimal("-1"))
        val violations = validator.validate(dto)
        assertThat(violations).anyMatch { it.propertyPath.toString() == "quantity" }
    }

    @Test
    fun `HoldingCreateRequest invalid negative avgCost`() {
        val dto = HoldingCreateRequest(symbol = "AAPL", avgCost = BigDecimal("-0.01"))
        val violations = validator.validate(dto)
        assertThat(violations).anyMatch { it.propertyPath.toString() == "avgCost" }
    }

    @Test
    fun `HoldingCreateRequest note over 200 chars is invalid`() {
        val longNote = "x".repeat(201)
        val dto = HoldingCreateRequest(symbol = "AAPL", note = longNote)
        val violations = validator.validate(dto)
        assertThat(violations).anyMatch { it.propertyPath.toString() == "note" }
    }

    @Test
    fun `HoldingCreateRequest zero quantity and zero avgCost are valid`() {
        val dto = HoldingCreateRequest(symbol = "AAPL", quantity = BigDecimal.ZERO, avgCost = BigDecimal.ZERO)
        val violations = validator.validate(dto)
        assertThat(violations).isEmpty()
    }


    @Test
    fun `UpdateRequest empty update is valid (no changes)`() {
        val dto = HoldingUpdateRequest()
        val violations = validator.validate(dto)
        assertThat(violations).isEmpty()
    }

    @Test
    fun `UpdateRequest valid positive quantity`() {
        val dto = HoldingUpdateRequest(quantity = BigDecimal("3.5"))
        val violations = validator.validate(dto)
        assertThat(violations).isEmpty()
    }

    @Test
    fun `UpdateRequest valid zero avgCost`() {
        val dto = HoldingUpdateRequest(avgCost = BigDecimal.ZERO)
        val violations = validator.validate(dto)
        assertThat(violations).isEmpty()
    }

    @Test
    fun `UpdateRequest invalid negative quantity`() {
        val dto = HoldingUpdateRequest(quantity = BigDecimal("-1"))
        val violations = validator.validate(dto)
        assertThat(violations).anyMatch { it.propertyPath.toString() == "quantity" }
    }

    @Test
    fun `UpdateRequest invalid negative avgCost`() {
        val dto = HoldingUpdateRequest(avgCost = BigDecimal("-0.01"))
        val violations = validator.validate(dto)
        assertThat(violations).anyMatch { it.propertyPath.toString() == "avgCost" }
    }

    @Test
    fun `UpdateRequest note over 200 chars is invalid`() {
        val dto = HoldingUpdateRequest(note = "x".repeat(201))
        val violations = validator.validate(dto)
        assertThat(violations).anyMatch { it.propertyPath.toString() == "note" }
    }
}