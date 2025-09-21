package com.notivest.portfolioservice.dto

import com.notivest.portfolioservice.dto.portfolio.request.PortfolioCreateRequest
import com.notivest.portfolioservice.dto.portfolio.request.PortfolioUpdateRequest
import jakarta.validation.Validation
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class PortfolioRequestDtoTest {
    companion object {
        private val factory = Validation.buildDefaultValidatorFactory()
        private val validator = factory.validator

        @JvmStatic
        @BeforeAll
        fun beforeAll() { /* no-op */ }

        @JvmStatic
        @AfterAll
        fun afterAll() {
            factory.close()
        }
    }

    @Test
    fun `valid create request`() {
        val dto = PortfolioCreateRequest(name = "My portfolio", baseCurrency = "USD")
        val violations = validator.validate(dto)
        assertThat(violations).isEmpty()
    }

    @Test
    fun `name too short`() {
        val dto = PortfolioCreateRequest(name = "ab", baseCurrency = "USD")
        val violations = validator.validate(dto)
        assertThat(violations).anyMatch { it.propertyPath.toString() == "name" }
    }

    @Test
    fun `name blank`() {
        val dto = PortfolioCreateRequest(name = "   ", baseCurrency = "USD")
        val violations = validator.validate(dto)
        assertThat(violations).anyMatch { it.propertyPath.toString() == "name" }
    }

    @Test
    fun `baseCurrency lowercase is invalid`() {
        val dto = PortfolioCreateRequest(name = "Okay name", baseCurrency = "usd")
        val violations = validator.validate(dto)
        assertThat(violations).anyMatch { it.propertyPath.toString() == "baseCurrency" }
    }

    @Test
    fun `baseCurrency wrong length is invalid`() {
        val dto = PortfolioCreateRequest(name = "Okay name", baseCurrency = "USDT")
        val violations = validator.validate(dto)
        assertThat(violations).anyMatch { it.propertyPath.toString() == "baseCurrency" }
    }

    @Test
    fun `empty update is valid (no changes)`() {
        val dto = PortfolioUpdateRequest()
        val violations = validator.validate(dto)
        assertThat(violations).isEmpty()
    }

    @Test
    fun `valid name update`() {
        val dto = PortfolioUpdateRequest(name = "Renamed portfolio")
        val violations = validator.validate(dto)
        assertThat(violations).isEmpty()
    }

    @Test
    fun `name too short on update`() {
        val dto = PortfolioUpdateRequest(name = "ab")
        val violations = validator.validate(dto)
        assertThat(violations).anyMatch { it.propertyPath.toString() == "name" }
    }

    @Test
    fun `name blank on update`() {
        val dto = PortfolioUpdateRequest(name = "  ")
        val violations = validator.validate(dto)
        assertThat(violations).anyMatch { it.propertyPath.toString() == "name" }
    }

    @Test
    fun `valid baseCurrency update`() {
        val dto = PortfolioUpdateRequest(baseCurrency = "EUR")
        val violations = validator.validate(dto)
        assertThat(violations).isEmpty()
    }

    @Test
    fun `invalid baseCurrency lowercase on update`() {
        val dto = PortfolioUpdateRequest(baseCurrency = "eur")
        val violations = validator.validate(dto)
        assertThat(violations).anyMatch { it.propertyPath.toString() == "baseCurrency" }
    }

    @Test
    fun `invalid baseCurrency length on update`() {
        val dto = PortfolioUpdateRequest(baseCurrency = "USDT")
        val violations = validator.validate(dto)
        assertThat(violations).anyMatch { it.propertyPath.toString() == "baseCurrency" }
    }
}
