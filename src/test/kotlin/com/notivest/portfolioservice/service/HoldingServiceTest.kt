package com.notivest.portfolioservice.service

import com.notivest.portfolioservice.dto.holding.request.HoldingCreateRequest
import com.notivest.portfolioservice.dto.holding.request.HoldingUpdateRequest
import com.notivest.portfolioservice.models.HoldingEntity
import com.notivest.portfolioservice.models.portfolio.PortfolioEntity
import com.notivest.portfolioservice.repository.HoldingRepository
import com.notivest.portfolioservice.repository.PortfolioRepository
import com.notivest.portfolioservice.service.implementations.HoldingServiceImpl
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal
import java.time.Instant
import java.util.*

class HoldingServiceTest {


    private val holdingRepo: HoldingRepository = mock(HoldingRepository::class.java)
    private val portfolioRepo: PortfolioRepository = mock(PortfolioRepository::class.java)
    private val service = HoldingServiceImpl(holdingRepo, portfolioRepo)

    private fun portfolio(userId: UUID, id: UUID = UUID.randomUUID()) =
        PortfolioEntity(id = id, userId = userId, name = "Main", baseCurrency = "USD").apply {
            createdAt = Instant.parse("2025-01-01T00:00:00Z")
            updatedAt = Instant.parse("2025-01-01T00:00:00Z")
        }

    @Test
    fun `create uppercases symbol and returns response`() {
        val userId = UUID.randomUUID()
        val p = portfolio(userId)
        `when`(portfolioRepo.findByIdAndUserIdAndDeletedAtIsNull(p.id!!, userId))
            .thenReturn(Optional.of(p))
        `when`(holdingRepo.saveAndFlush(any(HoldingEntity::class.java))).thenAnswer { inv ->
            val e = inv.arguments[0] as HoldingEntity
            e.id = UUID.randomUUID()
            e.createdAt = Instant.parse("2025-03-01T00:00:00Z")
            e.updatedAt = Instant.parse("2025-03-01T00:00:00Z")
            e
        }

        val out = service.create(userId, p.id!!, HoldingCreateRequest(symbol = "aapl"))

        assertThat(out.symbol).isEqualTo("AAPL")
        assertThat(out.id).isNotNull()
        verify(holdingRepo).saveAndFlush(any(HoldingEntity::class.java))
    }

    @Test
    fun `create maps unique violation to 409`() {
        val userId = UUID.randomUUID()
        val p = portfolio(userId)
        `when`(portfolioRepo.findByIdAndUserIdAndDeletedAtIsNull(p.id!!, userId))
            .thenReturn(Optional.of(p))
        `when`(holdingRepo.saveAndFlush(any(HoldingEntity::class.java)))
            .thenThrow(DataIntegrityViolationException("duplicate"))

        assertThatThrownBy {
            service.create(userId, p.id!!, HoldingCreateRequest(symbol = "MSFT"))
        }.isInstanceOf(ResponseStatusException::class.java)
            .hasMessageContaining("409")
    }

    @Test
    fun `list without filter uses plain find and maps to responses`() {
        val userId = UUID.randomUUID()
        val p = portfolio(userId)
        `when`(portfolioRepo.findByIdAndUserIdAndDeletedAtIsNull(p.id!!, userId))
            .thenReturn(Optional.of(p))

        val h1 = HoldingEntity(portfolio = p, symbol = "AAPL").apply {
            id = UUID.randomUUID(); createdAt = Instant.now(); updatedAt = Instant.now()
        }
        val h2 = HoldingEntity(portfolio= p, symbol= "MSFT").apply {
            id = UUID.randomUUID(); createdAt = Instant.now(); updatedAt = Instant.now()
        }

        `when`(holdingRepo.findAllByPortfolioId(p.id!!, PageRequest.of(0, 10)))
            .thenReturn(PageImpl(listOf(h1, h2)))

        val page = service.list(userId, p.id!!, null, PageRequest.of(0, 10))
        assertThat(page.totalElements).isEqualTo(2)
        assertThat(page.content.map { it.symbol }).containsExactlyInAnyOrder("AAPL", "MSFT")
    }

    @Test
    fun `list with filter uses containsIgnoreCase`() {
        val userId = UUID.randomUUID()
        val p = portfolio(userId)
        `when`(portfolioRepo.findByIdAndUserIdAndDeletedAtIsNull(p.id!!, userId))
            .thenReturn(Optional.of(p))

        val h1 = HoldingEntity(portfolio = p, symbol= "AAPL").apply {
            id = UUID.randomUUID(); createdAt = Instant.now(); updatedAt = Instant.now()
        }
        `when`(holdingRepo.findAllByPortfolioIdAndSymbolContainingIgnoreCase(p.id!!, "aap", PageRequest.of(0, 10)))
            .thenReturn(PageImpl(listOf(h1)))

        val page = service.list(userId, p.id!!, "aap", PageRequest.of(0, 10))
        assertThat(page.totalElements).isEqualTo(1)
        assertThat(page.content.map { it.symbol }).containsExactly("AAPL")
    }

    @Test
    fun `update changes only provided fields`() {
        val userId = UUID.randomUUID()
        val p = portfolio(userId)
        `when`(portfolioRepo.findByIdAndUserIdAndDeletedAtIsNull(p.id!!, userId))
            .thenReturn(Optional.of(p))

        val e = HoldingEntity(portfolio =p, symbol = "TSLA",
            quantity = BigDecimal("2"),
            avgCost = BigDecimal("100.00"),
            note = "Init"
        ).apply {
            id = UUID.randomUUID()
            createdAt = Instant.now(); updatedAt = Instant.now()
        }
        `when`(holdingRepo.findByIdAndPortfolioId(e.id!!, p.id!!))
            .thenReturn(Optional.of(e))
        `when`(holdingRepo.saveAndFlush(any(HoldingEntity::class.java))).thenAnswer { it.arguments[0] }

        val out = service.update(
            userId, p.id!!, e.id!!,
            HoldingUpdateRequest(quantity = BigDecimal("3.5"), avgCost = null, note = "Updated")
        )

        assertThat(out.symbol).isEqualTo("TSLA")
        assertThat(out.quantity).isEqualByComparingTo("3.5")
        assertThat(out.avgCost).isEqualByComparingTo("100.00") // unchanged
    }

    @Test
    fun `delete removes holding`() {
        val userId = UUID.randomUUID()
        val p = portfolio(userId)
        `when`(portfolioRepo.findByIdAndUserIdAndDeletedAtIsNull(p.id!!, userId))
            .thenReturn(Optional.of(p))

        val e = HoldingEntity(portfolio=p, symbol = "NVDA").apply {
            id = UUID.randomUUID(); createdAt = Instant.now(); updatedAt = Instant.now()
        }
        `when`(holdingRepo.findByIdAndPortfolioId(e.id!!, p.id!!))
            .thenReturn(Optional.of(e))

        service.delete(userId, p.id!!, e.id!!)
        verify(holdingRepo).delete(e)
    }
}
