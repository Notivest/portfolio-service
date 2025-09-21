package com.notivest.portfolioservice.dto

import com.notivest.portfolioservice.dto.holding.applyTo
import com.notivest.portfolioservice.dto.holding.request.HoldingCreateRequest
import com.notivest.portfolioservice.dto.holding.request.HoldingUpdateRequest
import com.notivest.portfolioservice.dto.holding.toEntity
import com.notivest.portfolioservice.dto.holding.toResponse
import com.notivest.portfolioservice.models.HoldingEntity
import com.notivest.portfolioservice.models.portfolio.PortfolioEntity
import com.notivest.portfolioservice.models.portfolio.PortfolioStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

class HoldingMapperTest {

    private fun portfolioWithId(): PortfolioEntity =
        PortfolioEntity(
            id = UUID.randomUUID(),
            userId = UUID.randomUUID(),
            name = "P",
            baseCurrency = "USD",
            status = PortfolioStatus.ACTIVE
        )

    @Test
    fun `toEntity from create sets fields (position)`() {
        val p = portfolioWithId()
        val req = HoldingCreateRequest(
            symbol = "AAPL",
            quantity = BigDecimal("5.0"),
            avgCost = BigDecimal("150.00"),
            note = "Long-term"
        )

        val e = req.toEntity(p)

        assertThat(e.portfolio).isEqualTo(p)
        assertThat(e.symbol).isEqualTo("AAPL")
        assertThat(e.quantity).isEqualTo(BigDecimal("5.0"))
        assertThat(e.avgCost).isEqualTo(BigDecimal("150.00"))
        assertThat(e.note).isEqualTo("Long-term")
        assertThat(e.id).isNull()
        assertThat(e.createdAt).isNull()
        assertThat(e.updatedAt).isNull()
    }

    @Test
    fun `toEntity from create supports watchlist (null quantity and avgCost)`() {
        val p = portfolioWithId()
        val req = HoldingCreateRequest(symbol = "RDS-A", note = "Watch")

        val e = req.toEntity(p)

        assertThat(e.quantity).isNull()
        assertThat(e.avgCost).isNull()
        assertThat(e.note).isEqualTo("Watch")
    }

    @Test
    fun `applyTo updates only provided fields`() {
        val p = portfolioWithId()
        val e = HoldingEntity(
            id = UUID.randomUUID(),
            portfolio = p,
            symbol = "MSFT",
            quantity = BigDecimal("2"),
            avgCost = BigDecimal("100.00"),
            note = "Init"
        ).apply {
            createdAt = Instant.parse("2025-03-01T00:00:00Z")
            updatedAt = Instant.parse("2025-03-02T00:00:00Z")
        }

        val update = HoldingUpdateRequest(
            quantity = BigDecimal("3.5"),
            avgCost = null,
            note = "Updated note"
        )

        update.applyTo(e)

        assertThat(e.symbol).isEqualTo("MSFT")              // unchanged
        assertThat(e.quantity).isEqualTo(BigDecimal("3.5")) // updated
        assertThat(e.avgCost).isEqualTo(BigDecimal("100.00")) // unchanged
        assertThat(e.note).isEqualTo("Updated note")
    }

    @Test
    fun `toResponse maps all fields`() {
        val p = portfolioWithId()
        val id = UUID.randomUUID()
        val e = HoldingEntity(
            id = id,
            portfolio = p,
            symbol = "BRK.B",
            quantity = BigDecimal("1"),
            avgCost = BigDecimal("400.00"),
            note = null
        ).apply {
            createdAt = Instant.parse("2025-04-01T00:00:00Z")
            updatedAt = Instant.parse("2025-04-02T00:00:00Z")
        }

        val dto = e.toResponse()

        assertThat(dto.id).isEqualTo(id)
        assertThat(dto.portfolioId).isEqualTo(p.id)
        assertThat(dto.symbol).isEqualTo("BRK.B")
        assertThat(dto.quantity).isEqualTo(BigDecimal("1"))
        assertThat(dto.avgCost).isEqualTo(BigDecimal("400.00"))
        assertThat(dto.createdAt).isEqualTo(e.createdAt)
        assertThat(dto.updatedAt).isEqualTo(e.updatedAt)
    }
}