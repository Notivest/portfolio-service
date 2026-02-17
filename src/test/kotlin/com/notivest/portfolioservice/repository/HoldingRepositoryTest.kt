package com.notivest.portfolioservice.repository

import com.notivest.portfolioservice.BaseIntegrationTest
import com.notivest.portfolioservice.models.HoldingEntity
import com.notivest.portfolioservice.models.portfolio.PortfolioEntity
import com.notivest.portfolioservice.models.portfolio.PortfolioStatus
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal
import java.util.UUID

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class HoldingRepositoryTest : BaseIntegrationTest() {
    @Autowired lateinit var holdingRepository: HoldingRepository

    @Autowired lateinit var portfolioRepository: PortfolioRepository

    private fun newPortfolio(): PortfolioEntity =
        portfolioRepository.saveAndFlush(
            PortfolioEntity(
                userId = UUID.randomUUID(),
                name = "My Portfolio",
                baseCurrency = "USD",
                status = PortfolioStatus.ACTIVE,
            ),
        )

    private fun addHolding(
        p: PortfolioEntity,
        symbol: String,
        quantity: BigDecimal? = null,
        avgCost: BigDecimal? = null,
        note: String? = null,
    ): HoldingEntity =
        holdingRepository.saveAndFlush(
            HoldingEntity(
                portfolio = p,
                symbol = symbol,
                quantity = quantity,
                avgCost = avgCost,
                note = note,
            ),
        )

    @Test
    fun `findAllByPortfolioId returns only that portfolio holdings and supports pagination`() {
        val p1 = newPortfolio()
        val p2 = newPortfolio()

        repeat(3) { addHolding(p1, "SYM$it") }
        repeat(2) { addHolding(p2, "OTH$it") }

        val page = holdingRepository.findAllByPortfolioId(p1.id!!, PageRequest.of(0, 10))

        assertThat(page.totalElements).isEqualTo(3)
        assertThat(page.content).allMatch { it.portfolio.id == p1.id }
    }

    @Test
    fun `findAllByPortfolioIdAndSymbolContainingIgnoreCase filters by substring case-insensitive`() {
        val p = newPortfolio()
        addHolding(p, "AAPL")
        addHolding(p, "MSFT")
        addHolding(p, "AAPD")

        val page =
            holdingRepository.findAllByPortfolioIdAndSymbolContainingIgnoreCase(
                p.id!!,
                "aap",
                PageRequest.of(0, 10),
            )

        assertThat(page.totalElements).isEqualTo(2)
        assertThat(page.content.map { it.symbol }).containsExactlyInAnyOrder("AAPL", "AAPD")
    }

    @Test
    fun `symbolsByPortfolioId returns distinct set of symbols`() {
        val p = newPortfolio()
        addHolding(p, "AAPL")
        addHolding(p, "MSFT")

        val p2 = newPortfolio()
        addHolding(p2, "AAPL")

        val symbols = holdingRepository.symbolsByPortfolioId(p.id!!)
        assertThat(symbols).containsExactlyInAnyOrder("AAPL", "MSFT")
    }

    @Test
    fun `unique constraint on (portfolio_id, symbol) raises DataIntegrityViolationException`() {
        val p = newPortfolio()
        addHolding(p, "TSLA")

        assertThatThrownBy {
            addHolding(p, "TSLA") // same portfolio + symbol should violate uq_holdings_portfolio_symbol
        }.isInstanceOf(DataIntegrityViolationException::class.java)
    }
}
