package com.notivest.portfolioservice.service

import com.notivest.portfolioservice.models.HoldingEntity
import com.notivest.portfolioservice.models.portfolio.PortfolioEntity
import com.notivest.portfolioservice.repository.HoldingRepository
import com.notivest.portfolioservice.repository.PortfolioRepository
import com.notivest.portfolioservice.service.implementations.InternalPortfolioServiceImpl
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

class InternalPortfolioServiceImplTest {
    private val portfolioRepository: PortfolioRepository = mock()
    private val holdingRepository: HoldingRepository = mock()
    private val service = InternalPortfolioServiceImpl(portfolioRepository, holdingRepository)

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

    private fun holding(
        portfolio: PortfolioEntity,
        symbol: String,
    ) = HoldingEntity(
        portfolio = portfolio,
        symbol = symbol,
        quantity = BigDecimal("10.0"),
        avgCost = BigDecimal("100.0"),
    ).apply {
        id = UUID.randomUUID()
        createdAt = Instant.parse("2024-01-02T00:00:00Z")
        updatedAt = Instant.parse("2024-01-02T00:00:00Z")
    }

    @Test
    fun `list returns portfolios without holdings when includeHoldings is false`() {
        val userId = UUID.randomUUID()
        val p1 = portfolio(UUID.randomUUID(), userId, "Retirement")
        val p2 = portfolio(UUID.randomUUID(), userId, "Savings")
        whenever(portfolioRepository.findAllByUserIdAndDeletedAtIsNull(userId)).thenReturn(listOf(p1, p2))

        val out = service.listPortfoliosForUser(userId, includeHoldings = false)

        assertThat(out).hasSize(2)
        assertThat(out.all { it.holdings == null }).isTrue()
        verify(portfolioRepository).findAllByUserIdAndDeletedAtIsNull(userId)
        verifyNoInteractions(holdingRepository)
    }

    @Test
    fun `list returns holdings grouped by portfolio when includeHoldings is true`() {
        val userId = UUID.randomUUID()
        val p1 = portfolio(UUID.randomUUID(), userId, "Growth")
        val p2 = portfolio(UUID.randomUUID(), userId, "Income")
        val h1 = holding(p1, "AAPL")
        val h2 = holding(p1, "MSFT")
        val h3 = holding(p2, "BTC")

        whenever(portfolioRepository.findAllByUserIdAndDeletedAtIsNull(userId)).thenReturn(listOf(p1, p2))
        whenever(holdingRepository.findByUserId(userId)).thenReturn(listOf(h1, h2, h3))

        val out = service.listPortfoliosForUser(userId, includeHoldings = true)

        assertThat(out).hasSize(2)
        val growth = out.first { it.id == p1.id }
        assertThat(growth.holdings).hasSize(2)
        assertThat(growth.holdings?.map { it.symbol }).containsExactlyInAnyOrder("AAPL", "MSFT")
        assertThat(growth.holdings?.all { it.marketValue != null }).isTrue()
        assertThat(growth.holdings?.all { it.asOf != null }).isTrue()
        assertThat(growth.holdings?.first { it.symbol == "AAPL" }?.marketValue).isEqualByComparingTo("1000.0")
        assertThat(growth.holdings?.first { it.symbol == "AAPL" }?.asOf).isEqualTo(Instant.parse("2024-01-02T00:00:00Z"))

        val income = out.first { it.id == p2.id }
        assertThat(income.holdings).hasSize(1)
        assertThat(income.holdings?.first()?.symbol).isEqualTo("BTC")
        assertThat(income.holdings?.first()?.marketValue).isEqualByComparingTo("1000.0")
        assertThat(income.holdings?.first()?.asOf).isEqualTo(Instant.parse("2024-01-02T00:00:00Z"))

        verify(portfolioRepository).findAllByUserIdAndDeletedAtIsNull(userId)
        verify(holdingRepository).findByUserId(userId)
    }
}
