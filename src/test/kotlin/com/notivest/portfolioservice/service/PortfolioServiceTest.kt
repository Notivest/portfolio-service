package com.notivest.portfolioservice.service

import com.notivest.portfolioservice.dto.portfolio.request.PortfolioCreateRequest
import com.notivest.portfolioservice.dto.portfolio.request.PortfolioUpdateRequest
import com.notivest.portfolioservice.dto.portfolio.response.PortfolioResponse
import com.notivest.portfolioservice.exception.NotFoundException
import com.notivest.portfolioservice.models.portfolio.PortfolioEntity
import com.notivest.portfolioservice.models.portfolio.PortfolioStatus
import com.notivest.portfolioservice.repository.PortfolioRepository
import com.notivest.portfolioservice.service.implementations.PortfolioServiceImpl
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.verify
import org.mockito.kotlin.verify
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.time.Instant
import java.util.*

class PortfolioServiceTest {
    @MockK
    lateinit var portfolioRepository: PortfolioRepository

    // ⚠️ Ajusta el nombre del impl si es distinto
    @InjectMockKs
    lateinit var service: PortfolioServiceImpl

    private val userId = UUID.randomUUID()
    private val now = Instant.parse("2025-03-01T00:00:00Z")

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        clearMocks(portfolioRepository)
    }

    // ---------- Helpers ----------
    private fun entity(
        id: UUID = UUID.randomUUID(),
        deleted: Boolean = false,
    ) = PortfolioEntity(
        id = id,
        userId = userId,
        name = "Main",
        baseCurrency = "USD",
        status = PortfolioStatus.ACTIVE,
    ).apply {
        createdAt = now
        updatedAt = now
        if (deleted) deletedAt = now
    }

    // ---------- Tests ----------

    @Test
    fun `list returns paged responses for user and filters soft-deleted`() {
        val pageable = PageRequest.of(0, 2)
        val e1 = entity()
        val e2 = entity()

        every { portfolioRepository.findAllByUserIdAndDeletedAtIsNull(userId, pageable) } returns PageImpl(listOf(e1, e2), pageable, 2)

        val page = service.list(userId, pageable)

        assertThat(page).isInstanceOf(Page::class.java)
        assertThat(page.totalElements).isEqualTo(2)
        assertThat(page.content.map(PortfolioResponse::id)).containsExactly(e1.id, e2.id)
        verify(exactly = 1) { portfolioRepository.findAllByUserIdAndDeletedAtIsNull(userId, pageable) }
    }

    @Test
    fun `get returns one portfolio if belongs to user and not soft-deleted`() {
        val id = UUID.randomUUID()
        val e = entity(id)

        every { portfolioRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId) } returns Optional.of(e)

        val out = service.get(userId, id)

        assertThat(out.id).isEqualTo(id)
        assertThat(out.userId).isEqualTo(userId)
        verify(exactly = 1) { portfolioRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId) }
    }

    @Test
    fun `get throws NOT_FOUND when portfolio does not exist or belongs to another user`() {
        val id = UUID.randomUUID()
        every { portfolioRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId) } returns Optional.empty()

        val ex = assertThrows<NotFoundException> { service.get(userId, id) }
        assertThat(ex.message).isEqualTo("Portfolio not found")

        verify(exactly = 1) { portfolioRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId) }
    }

    @Test
    fun `create saves entity and returns response with id and audit fields`() {
        val req = PortfolioCreateRequest(name = "New P", baseCurrency = "ARS")

        val saved =
            entity().apply {
                name = "New P"
                baseCurrency = "ARS"
            }

        every { portfolioRepository.saveAndFlush(any<PortfolioEntity>()) } returns saved

        val out = service.create(userId, req)

        assertThat(out.id).isEqualTo(saved.id)
        assertThat(out.name).isEqualTo("New P")
        assertThat(out.baseCurrency).isEqualTo("ARS")
        assertThat(out.userId).isEqualTo(userId)
        verify(exactly = 1) { portfolioRepository.saveAndFlush(any<PortfolioEntity>()) }
    }

    @Test
    fun `update applies only provided fields and returns updated response`() {
        val id = UUID.randomUUID()
        val existing =
            entity(id).apply {
                name = "Old"
                baseCurrency = "USD"
            }

        every { portfolioRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId) } returns Optional.of(existing)
        every { portfolioRepository.saveAndFlush(existing) } returns
            existing.apply {
                name = "New"
                // baseCurrency unchanged
                updatedAt = now.plusSeconds(60)
            }

        val out = service.update(userId, id, PortfolioUpdateRequest(name = "New", baseCurrency = null))

        assertThat(out.id).isEqualTo(id)
        assertThat(out.name).isEqualTo("New")
        assertThat(out.baseCurrency).isEqualTo("USD")
        verify { portfolioRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId) }
        verify { portfolioRepository.saveAndFlush(existing) }
    }

    @Test
    fun `delete performs soft-delete and persists`() {
        val id = UUID.randomUUID()
        val existing = entity(id)

        every { portfolioRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId) } returns Optional.of(existing)

        // capture saved entity to assert deletedAt set
        val savedSlot: CapturingSlot<PortfolioEntity> = slot()
        every { portfolioRepository.save(capture(savedSlot)) } answers { savedSlot.captured }

        service.delete(userId, id)

        verify { portfolioRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId) }
        verify { portfolioRepository.save(any()) }
        assertThat(savedSlot.captured.deletedAt).isNotNull()
    }
}
