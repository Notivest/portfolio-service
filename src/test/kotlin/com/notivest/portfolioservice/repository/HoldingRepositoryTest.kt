package com.notivest.portfolioservice.repository

import com.notivest.portfolioservice.models.HoldingEntity
import com.notivest.portfolioservice.models.portfolio.PortfolioEntity
import com.notivest.portfolioservice.models.portfolio.PortfolioStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
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
class HoldingRepositoryTest {

    @Autowired lateinit var portfolioRepository: PortfolioRepository
    @Autowired lateinit var holdingRepository: HoldingRepository

    private fun newPortfolio(userId: UUID = UUID.randomUUID()) =
        portfolioRepository.saveAndFlush(
            PortfolioEntity(
                userId = userId, name = "Main", baseCurrency = "USD", status = PortfolioStatus.ACTIVE
            )
        )

    private fun newHolding(p: PortfolioEntity, symbol: String, qty: BigDecimal) =
        holdingRepository.saveAndFlush(
            HoldingEntity(
                portfolio = p, symbol = symbol, quantity = qty, avgCost = BigDecimal("10.00")
            )
        )

    @Test
    fun `findAllByPortfolio_Id devuelve todo y la version paginada pagina`() {
        val p = newPortfolio()
        repeat(3) { i -> newHolding(p, "SYM$i", BigDecimal.ONE) }

        val all = holdingRepository.findAllByPortfolioId(p.id!!)
        assertThat(all).hasSize(3)

        val page = holdingRepository.findAllByPortfolioId(p.id!!, PageRequest.of(0, 2))
        assertThat(page.totalElements).isEqualTo(3)
        assertThat(page.content).hasSize(2)
    }

    @Test
    fun `findAllByPortfolio_IdAndSymbolContainingIgnoreCase busca substring case-insensitive`() {
        val p = newPortfolio()
        newHolding(p, "AAPL", BigDecimal("5"))
        newHolding(p, "AA", BigDecimal("3"))
        newHolding(p, "MSFT", BigDecimal("2"))

        val page = holdingRepository.findAllByPortfolioIdAndSymbolContainingIgnoreCase(
            p.id!!, "aa", PageRequest.of(0, 10)
        )
        assertThat(page.content.map { it.symbol }).containsExactlyInAnyOrder("AAPL", "AA")
        assertThat(page.totalElements).isEqualTo(2)
    }

    @Test
    fun `symbolsByPortfolioId retorna set distinct`() {
        val p = newPortfolio()
        newHolding(p, "AAPL", BigDecimal("1"))
        newHolding(p, "MSFT", BigDecimal("2"))

        val set = holdingRepository.symbolsByPortfolioId(p.id!!)
        assertThat(set).containsExactlyInAnyOrder("AAPL", "MSFT")
    }

    @Test
    fun `unicidad portfolio+symbol dispara DataIntegrityViolationException`() {
        val p = newPortfolio()
        newHolding(p, "AAPL", BigDecimal("1"))

        assertThrows<DataIntegrityViolationException> {
            holdingRepository.saveAndFlush(
                HoldingEntity(
                    portfolio = p,
                    symbol = "AAPL",
                    quantity = BigDecimal("2"),
                    avgCost = BigDecimal("20.00"),
                )
            )
        }
    }
}