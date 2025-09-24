package com.notivest.portfolioservice.service.integration

import com.notivest.portfolioservice.dto.holding.request.HoldingCreateRequest
import com.notivest.portfolioservice.dto.holding.request.HoldingUpdateRequest
import com.notivest.portfolioservice.models.HoldingEntity
import com.notivest.portfolioservice.models.portfolio.PortfolioEntity
import com.notivest.portfolioservice.models.portfolio.PortfolioStatus
import com.notivest.portfolioservice.repository.HoldingRepository
import com.notivest.portfolioservice.repository.PortfolioRepository
import com.notivest.portfolioservice.service.interfaces.HoldingService
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal
import java.util.UUID

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class HoldingServiceIT {

    @Autowired lateinit var service: HoldingService
    @Autowired lateinit var portfolioRepository: PortfolioRepository
    @Autowired lateinit var holdingRepository: HoldingRepository

    private fun newPortfolioFor(userId: UUID) =
        portfolioRepository.saveAndFlush(
            com.notivest.portfolioservice.models.portfolio.PortfolioEntity(
                userId = userId,
                name = "Main",
                baseCurrency = "USD",
            )
        )

    @Test
    fun `create normalizes symbol to uppercase and persists`() {
        val userId = UUID.randomUUID()
        val p = newPortfolioFor(userId)

        val created = service.create(userId, p.id!!, HoldingCreateRequest(symbol = "aapl"))
        assertThat(created.symbol).isEqualTo("AAPL")
        assertThat(created.id).isNotNull

        // createdAt set by DB because service uses saveAndFlush()
        assertThat(created.createdAt).isNotNull
        assertThat(created.updatedAt).isNotNull
    }

    @Test
    fun `creating a duplicate symbol in same portfolio returns 409`() {
        val userId = UUID.randomUUID()
        val p = newPortfolioFor(userId)

        service.create(userId, p.id!!, HoldingCreateRequest(symbol = "msft"))
        assertThatThrownBy {
            service.create(userId, p.id!!, HoldingCreateRequest(symbol = "MSFT"))
        }.isInstanceOf(ResponseStatusException::class.java)
            .message().contains("409")
    }

    @Test
    fun `list supports filter by symbol substring (case-insensitive) and pagination`() {
        val userId = UUID.randomUUID()
        val p = newPortfolioFor(userId)
        service.create(userId, p.id!!, HoldingCreateRequest(symbol = "AAPL"))
        service.create(userId, p.id!!, HoldingCreateRequest(symbol = "AAPD"))
        service.create(userId, p.id!!, HoldingCreateRequest(symbol = "MSFT"))

        val page = service.list(userId, p.id!!, "aap", org.springframework.data.domain.PageRequest.of(0, 10))
        assertThat(page.totalElements).isEqualTo(2)
        assertThat(page.content.map { it.symbol }).containsExactlyInAnyOrder("AAPL", "AAPD")
    }

    @Test
    fun `update changes only provided fields`() {
        val userId = UUID.randomUUID()
        val p = newPortfolioFor(userId)

        val created = service.create(
            userId, p.id!!,
            HoldingCreateRequest(symbol = "TSLA", quantity = BigDecimal("2"), avgCost = BigDecimal("100.00"), note = "Init")
        )

        val updated = service.update(
            userId, p.id!!, created.id,
            HoldingUpdateRequest(quantity = BigDecimal("3.5"), avgCost = null, note = "Updated")
        )

        assertThat(updated.symbol).isEqualTo("TSLA")
        assertThat(updated.quantity).isEqualByComparingTo(BigDecimal("3.5"))
        assertThat(updated.avgCost).isEqualByComparingTo(BigDecimal("100.00"))
    }

    @Test
    fun `delete removes the holding`() {
        val userId = UUID.randomUUID()
        val p = newPortfolioFor(userId)
        val created = service.create(userId, p.id!!, HoldingCreateRequest(symbol = "NVDA"))

        service.delete(userId, p.id!!, created.id)

        val stillThere = holdingRepository.findById(created.id)
        assertThat(stillThere).isEmpty
    }

    @Test
    fun `watchlist is allowed (quantity null)`() {
        val userId = UUID.randomUUID()
        val p = newPortfolioFor(userId)

        val created = service.create(userId, p.id!!, HoldingCreateRequest(symbol = "COIN", quantity = null))
        assertThat(created.quantity).isNull()
    }
}