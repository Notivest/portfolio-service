package com.notivest.portfolioservice.dto

import com.notivest.portfolioservice.dto.portfolio.applyTo
import com.notivest.portfolioservice.dto.portfolio.request.PortfolioCreateRequest
import com.notivest.portfolioservice.dto.portfolio.request.PortfolioUpdateRequest
import com.notivest.portfolioservice.dto.portfolio.toEntity
import com.notivest.portfolioservice.dto.portfolio.toResponse
import com.notivest.portfolioservice.models.portfolio.PortfolioEntity
import com.notivest.portfolioservice.models.portfolio.PortfolioStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class PortfolioMapperTest {
    @Test
    fun `toEntity from create sets fields and defaults`() {
        val userId = UUID.randomUUID()
        val req = PortfolioCreateRequest(name = "My Portfolio", baseCurrency = "USD")

        val entity = req.toEntity(userId)

        assertThat(entity.userId).isEqualTo(userId)
        assertThat(entity.name).isEqualTo("My Portfolio")
        assertThat(entity.baseCurrency).isEqualTo("USD")
        assertThat(entity.status).isEqualTo(PortfolioStatus.ACTIVE) // default from entity
        assertThat(entity.id).isNull()
        assertThat(entity.createdAt).isNull()
        assertThat(entity.updatedAt).isNull()
    }

    @Test
    fun `applyTo only updates non-null fields`() {
        val entity =
            PortfolioEntity(
                id = UUID.randomUUID(),
                userId = UUID.randomUUID(),
                name = "Old Name",
                baseCurrency = "USD",
            ).apply {
                createdAt = Instant.parse("2025-01-01T00:00:00Z")
                updatedAt = Instant.parse("2025-01-02T00:00:00Z")
            }

        // Only change name
        val update = PortfolioUpdateRequest(name = "New Name", baseCurrency = null)
        update.applyTo(entity)

        assertThat(entity.name).isEqualTo("New Name")
        assertThat(entity.baseCurrency).isEqualTo("USD") // unchanged
        assertThat(entity.status).isEqualTo(PortfolioStatus.ACTIVE) // untouched
    }

    @Test
    fun `toResponse maps all fields`() {
        val id = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val entity =
            PortfolioEntity(
                id = id,
                userId = userId,
                name = "My Portfolio",
                baseCurrency = "EUR",
            ).apply {
                createdAt = Instant.parse("2025-02-01T00:00:00Z")
                updatedAt = Instant.parse("2025-02-02T00:00:00Z")
            }

        val dto = entity.toResponse()

        assertThat(dto.id).isEqualTo(id)
        assertThat(dto.userId).isEqualTo(userId)
        assertThat(dto.name).isEqualTo("My Portfolio")
        assertThat(dto.baseCurrency).isEqualTo("EUR")
        assertThat(dto.status).isEqualTo(entity.status)
        assertThat(dto.createdAt).isEqualTo(entity.createdAt)
        assertThat(dto.updatedAt).isEqualTo(entity.updatedAt)
    }
}
