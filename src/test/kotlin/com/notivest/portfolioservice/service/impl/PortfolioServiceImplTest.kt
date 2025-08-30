package com.notivest.portfolioservice.service.impl

import com.notivest.portfolioservice.models.PortfolioEntity
import com.notivest.portfolioservice.repository.PortfolioRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.time.OffsetDateTime
import java.util.Optional
import java.util.UUID

class PortfolioServiceImplTest {
    private val portfolioRepository: PortfolioRepository = mock()
    private val service = PortfolioServiceImpl(portfolioRepository)

    @Test
    fun `should create portfolio successfully`() {
        val userId = "testuser"
        val name = "Test Portfolio"
        val baseCurrency = "usd"
        val expectedEntity =
            PortfolioEntity(
                id = UUID.randomUUID(),
                userId = userId,
                name = name,
                baseCurrency = "USD",
            )

        whenever(portfolioRepository.save(any<PortfolioEntity>())).thenReturn(expectedEntity)

        val result = service.create(userId, name, baseCurrency)

        assertEquals(expectedEntity, result)
        assertEquals("USD", result.baseCurrency) // Should be uppercase
        verify(portfolioRepository).save(any<PortfolioEntity>())
    }

    @Test
    fun `should throw exception when currency is invalid`() {
        val userId = "testuser"
        val name = "Test Portfolio"
        val invalidCurrency = "INVALID"

        assertThrows<IllegalArgumentException> {
            service.create(userId, name, invalidCurrency)
        }

        verify(portfolioRepository, never()).save(any<PortfolioEntity>())
    }

    @Test
    fun `should get portfolio by id and user`() {
        val userId = "testuser"
        val portfolioId = UUID.randomUUID()
        val expectedEntity =
            PortfolioEntity(
                id = portfolioId,
                userId = userId,
                name = "Test Portfolio",
                baseCurrency = "USD",
            )

        whenever(portfolioRepository.findById(portfolioId)).thenReturn(Optional.of(expectedEntity))

        val result = service.get(userId, portfolioId)

        assertEquals(expectedEntity, result)
        verify(portfolioRepository).findById(portfolioId)
    }

    @Test
    fun `should throw exception when portfolio not found`() {
        val userId = "testuser"
        val portfolioId = UUID.randomUUID()

        whenever(portfolioRepository.findById(portfolioId)).thenReturn(Optional.empty())

        assertThrows<NoSuchElementException> {
            service.get(userId, portfolioId)
        }
    }

    @Test
    fun `should throw exception when portfolio belongs to different user`() {
        val userId = "testuser"
        val otherUserId = "otheruser"
        val portfolioId = UUID.randomUUID()
        val entity =
            PortfolioEntity(
                id = portfolioId,
                userId = otherUserId,
                name = "Other's Portfolio",
                baseCurrency = "USD",
            )

        whenever(portfolioRepository.findById(portfolioId)).thenReturn(Optional.of(entity))

        assertThrows<NoSuchElementException> {
            service.get(userId, portfolioId)
        }
    }

    @Test
    fun `should list portfolios for user`() {
        val userId = "testuser"
        val page = 0
        val size = 10
        val portfolios =
            listOf(
                PortfolioEntity(
                    id = UUID.randomUUID(),
                    userId = userId,
                    name = "Portfolio 1",
                    baseCurrency = "USD",
                ),
                PortfolioEntity(
                    id = UUID.randomUUID(),
                    userId = userId,
                    name = "Portfolio 2",
                    baseCurrency = "EUR",
                ),
            )
        val expectedPage = PageImpl(portfolios, PageRequest.of(page, size), portfolios.size.toLong())

        whenever(portfolioRepository.findAllByUserId(userId, PageRequest.of(page, size)))
            .thenReturn(expectedPage)

        val result = service.list(userId, page, size)

        assertEquals(expectedPage, result)
        assertEquals(2, result.content.size)
        verify(portfolioRepository).findAllByUserId(userId, PageRequest.of(page, size))
    }

    @Test
    fun `should update portfolio name and currency`() {
        val userId = "testuser"
        val portfolioId = UUID.randomUUID()
        val originalEntity =
            PortfolioEntity(
                id = portfolioId,
                userId = userId,
                name = "Original Name",
                baseCurrency = "USD",
            )
        val updatedEntity = originalEntity.copy()

        whenever(portfolioRepository.findById(portfolioId)).thenReturn(Optional.of(originalEntity))
        whenever(portfolioRepository.save(any<PortfolioEntity>())).thenReturn(updatedEntity)

        val result = service.update(userId, portfolioId, "New Name", "eur")

        assertEquals("New Name", originalEntity.name)
        assertEquals("EUR", originalEntity.baseCurrency)
        assertNotNull(originalEntity.updatedAt)
        verify(portfolioRepository).save(originalEntity)
    }

    @Test
    fun `should update only name when currency is null`() {
        val userId = "testuser"
        val portfolioId = UUID.randomUUID()
        val originalEntity =
            PortfolioEntity(
                id = portfolioId,
                userId = userId,
                name = "Original Name",
                baseCurrency = "USD",
            )

        whenever(portfolioRepository.findById(portfolioId)).thenReturn(Optional.of(originalEntity))
        whenever(portfolioRepository.save(any<PortfolioEntity>())).thenReturn(originalEntity)

        service.update(userId, portfolioId, "New Name", null)

        assertEquals("New Name", originalEntity.name)
        assertEquals("USD", originalEntity.baseCurrency) // Should remain unchanged
    }

    @Test
    fun `should delete portfolio`() {
        val userId = "testuser"
        val portfolioId = UUID.randomUUID()
        val entity =
            PortfolioEntity(
                id = portfolioId,
                userId = userId,
                name = "Test Portfolio",
                baseCurrency = "USD",
            )

        whenever(portfolioRepository.findById(portfolioId)).thenReturn(Optional.of(entity))

        service.delete(userId, portfolioId)

        verify(portfolioRepository).delete(entity)
    }
}
