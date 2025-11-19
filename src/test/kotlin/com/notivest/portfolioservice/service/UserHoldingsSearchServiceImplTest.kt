package com.notivest.portfolioservice.service

import com.notivest.portfolioservice.models.HoldingEntity
import com.notivest.portfolioservice.models.portfolio.PortfolioEntity
import com.notivest.portfolioservice.repository.HoldingRepository
import com.notivest.portfolioservice.service.implementations.UserHoldingsSearchServiceImpl
import jakarta.validation.ConstraintViolationException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

class UserHoldingsSearchServiceImplTest {
    private val holdingRepository: HoldingRepository = mock(HoldingRepository::class.java)
    private val service = UserHoldingsSearchServiceImpl(holdingRepository)

    private fun portfolio(
        id: UUID,
        userId: UUID,
        name: String,
    ) = PortfolioEntity(
        id = id,
        userId = userId,
        name = name,
        baseCurrency = "USD",
    ).apply {
        createdAt = Instant.parse("2024-01-01T00:00:00Z")
        updatedAt = Instant.parse("2024-01-01T00:00:00Z")
    }

    @Test
    fun `search normalizes symbols and returns holdings`() {
        val userId = UUID.randomUUID()
        val p1 = portfolio(UUID.randomUUID(), userId, "Growth")
        val p2 = portfolio(UUID.randomUUID(), userId, "Income")

        val holdingAapl =
            HoldingEntity(
                portfolio = p1,
                symbol = "AAPL",
                quantity = BigDecimal("10.5"),
                avgCost = BigDecimal("145.20"),
            ).apply {
                id = UUID.randomUUID()
                createdAt = Instant.parse("2024-01-05T00:00:00Z")
                updatedAt = Instant.parse("2024-02-05T00:00:00Z")
            }
        val holdingMsft =
            HoldingEntity(
                portfolio = p2,
                symbol = "MSFT",
                quantity = BigDecimal("3"),
                avgCost = BigDecimal("320.00"),
            ).apply {
                id = UUID.randomUUID()
                createdAt = Instant.parse("2024-01-06T00:00:00Z")
                updatedAt = Instant.parse("2024-02-06T00:00:00Z")
            }

        whenever(holdingRepository.findByUserIdAndSymbols(userId, listOf("AAPL", "MSFT")))
            .thenReturn(listOf(holdingAapl, holdingMsft))

        val out = service.search(userId, listOf(" aapl ", "MSFT", "aapl"))

        assertThat(out).hasSize(2)
        assertThat(out.map { it.symbol }).containsExactlyInAnyOrder("AAPL", "MSFT")
        assertThat(out.first { it.symbol == "AAPL" }.portfolioName).isEqualTo("Growth")
        assertThat(out.first { it.symbol == "MSFT" }.portfolioId).isEqualTo(p2.id)

        verify(holdingRepository).findByUserIdAndSymbols(userId, listOf("AAPL", "MSFT"))
    }

    @Test
    fun `search throws not found when no holdings match`() {
        val userId = UUID.randomUUID()
        whenever(holdingRepository.findByUserIdAndSymbols(userId, listOf("TSLA")))
            .thenReturn(emptyList())

        assertThatThrownBy { service.search(userId, listOf("TSLA")) }
            .isInstanceOf(com.notivest.portfolioservice.exception.NotFoundException::class.java)
    }

    @Test
    fun `search rejects blank symbols`() {
        val userId = UUID.randomUUID()

        assertThatThrownBy { service.search(userId, listOf("  ")) }
            .isInstanceOf(ConstraintViolationException::class.java)
            .hasMessageContaining("symbols[0] must not be blank")
    }

    @Test
    fun `search rejects more than twenty symbols`() {
        val userId = UUID.randomUUID()
        val symbols = (1..21).map { "SYM$it" }

        assertThatThrownBy { service.search(userId, symbols) }
            .isInstanceOf(ConstraintViolationException::class.java)
            .hasMessageContaining("symbols must contain between 1 and 20 entries")
    }
}
