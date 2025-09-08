package com.notivest.portfolioservice.service.impl

import com.fasterxml.jackson.databind.ObjectMapper
import com.notivest.portfolioservice.models.PortfolioEntity
import com.notivest.portfolioservice.models.PositionEntity
import com.notivest.portfolioservice.models.ValuationEntity
import com.notivest.portfolioservice.repository.PositionRepository
import com.notivest.portfolioservice.repository.ValuationRepository
import com.notivest.portfolioservice.service.interfaces.PortfolioService
import com.notivest.portfolioservice.valuation.ValuationCalculator
import com.notivest.portfolioservice.valuation.models.PositionInput
import com.notivest.portfolioservice.valuation.models.ValuationResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.UUID

class ValuationServiceImplTest {
    private val portfolioService: PortfolioService = mock()
    private val positionRepository: PositionRepository = mock()
    private val valuationRepository: ValuationRepository = mock()
    private val calculator: ValuationCalculator = mock()
    private val mapper: ObjectMapper = mock()

    private val service =
        ValuationServiceImpl(
            portfolioService,
            positionRepository,
            valuationRepository,
            calculator,
            mapper,
        )

    private val userId = "testuser"
    private val portfolioId = UUID.randomUUID()
    private val accountId = UUID.randomUUID()
    private val testPortfolio =
        PortfolioEntity(
            id = portfolioId,
            userId = userId,
            name = "Test Portfolio",
            baseCurrency = "USD",
        )

    @Test
    fun `should run valuation successfully`() {
        val asOf = OffsetDateTime.now()
        val positions =
            listOf(
                PositionEntity(
                    id = UUID.randomUUID(),
                    portfolioId = portfolioId,
                    accountId = accountId,
                    symbolId = "AAPL",
                    qty = BigDecimal("100"),
                    avgCost = BigDecimal("150.00"),
                    currency = "USD",
                ),
                PositionEntity(
                    id = UUID.randomUUID(),
                    portfolioId = portfolioId,
                    accountId = accountId,
                    symbolId = "GOOGL",
                    qty = BigDecimal("50"),
                    avgCost = BigDecimal("2500.00"),
                    currency = "USD",
                ),
            )

        val valuationResult =
            ValuationResult(
                nav = BigDecimal("140000.00"),
                positions = emptyList(),
                fxUsed = emptyMap(),
            )

        val savedValuation =
            ValuationEntity(
                id = UUID.randomUUID(),
                portfolioId = portfolioId,
                asOf = asOf,
                totalsJson = "{\"NAV\":140000.00}",
                positionsJson = "[]",
                fxUsedJson = "{}",
            )

        whenever(portfolioService.get(userId, portfolioId)).thenReturn(testPortfolio)
        whenever(positionRepository.findAllByPortfolioId(portfolioId)).thenReturn(positions)
        whenever(calculator.compute(any<List<PositionInput>>(), any()))
            .thenReturn(valuationResult)
        whenever(mapper.writeValueAsString(any())).thenReturn("{}")
        whenever(valuationRepository.save(any<ValuationEntity>())).thenReturn(savedValuation)

        val result = service.runValuation(userId, portfolioId, asOf)

        assertNotNull(result)
        assertEquals(portfolioId, result.portfolioId)
        assertEquals(asOf, result.asOf)
        verify(portfolioService).get(userId, portfolioId)
        verify(positionRepository).findAllByPortfolioId(portfolioId)
        verify(calculator).compute(any<List<PositionInput>>(), any())
        verify(valuationRepository).save(any<ValuationEntity>())
    }

    @Test
    fun `should run valuation with empty positions`() {
        val asOf = OffsetDateTime.now()
        val positions = emptyList<PositionEntity>()

        val valuationResult =
            ValuationResult(
                nav = BigDecimal.ZERO,
                positions = emptyList(),
                fxUsed = emptyMap(),
            )

        val savedValuation =
            ValuationEntity(
                id = UUID.randomUUID(),
                portfolioId = portfolioId,
                asOf = asOf,
                totalsJson = "{\"NAV\":0}",
                positionsJson = "[]",
                fxUsedJson = "{}",
            )

        whenever(portfolioService.get(userId, portfolioId)).thenReturn(testPortfolio)
        whenever(positionRepository.findAllByPortfolioId(portfolioId)).thenReturn(positions)
        whenever(calculator.compute(any<List<PositionInput>>(), any()))
            .thenReturn(valuationResult)
        whenever(mapper.writeValueAsString(any())).thenReturn("{}")
        whenever(valuationRepository.save(any<ValuationEntity>())).thenReturn(savedValuation)

        val result = service.runValuation(userId, portfolioId, asOf)

        assertNotNull(result)
        assertEquals(portfolioId, result.portfolioId)
        verify(calculator).compute(emptyList(), "USD")
    }

    @Test
    fun `should get latest valuation`() {
        val latestValuation =
            ValuationEntity(
                id = UUID.randomUUID(),
                portfolioId = portfolioId,
                asOf = OffsetDateTime.now(),
                totalsJson = "{\"NAV\":100000.00}",
                positionsJson = "[]",
                fxUsedJson = "{}",
            )

        whenever(portfolioService.get(userId, portfolioId)).thenReturn(testPortfolio)
        whenever(valuationRepository.findTop1ByPortfolioIdOrderByAsOfDesc(portfolioId))
            .thenReturn(latestValuation)

        val result = service.latest(userId, portfolioId)

        assertEquals(latestValuation, result)
        verify(portfolioService).get(userId, portfolioId)
        verify(valuationRepository).findTop1ByPortfolioIdOrderByAsOfDesc(portfolioId)
    }

    @Test
    fun `should return null when no latest valuation exists`() {
        whenever(portfolioService.get(userId, portfolioId)).thenReturn(testPortfolio)
        whenever(valuationRepository.findTop1ByPortfolioIdOrderByAsOfDesc(portfolioId))
            .thenReturn(null)

        val result = service.latest(userId, portfolioId)

        assertNull(result)
    }

    @Test
    fun `should get valuation history`() {
        val from = OffsetDateTime.now().minusDays(7)
        val to = OffsetDateTime.now()
        val valuations =
            listOf(
                ValuationEntity(
                    id = UUID.randomUUID(),
                    portfolioId = portfolioId,
                    asOf = from.plusDays(1),
                    totalsJson = "{\"NAV\":95000.00}",
                    positionsJson = "[]",
                    fxUsedJson = "{}",
                ),
                ValuationEntity(
                    id = UUID.randomUUID(),
                    portfolioId = portfolioId,
                    asOf = from.plusDays(3),
                    totalsJson = "{\"NAV\":98000.00}",
                    positionsJson = "[]",
                    fxUsedJson = "{}",
                ),
                ValuationEntity(
                    id = UUID.randomUUID(),
                    portfolioId = portfolioId,
                    asOf = from.plusDays(5),
                    totalsJson = "{\"NAV\":102000.00}",
                    positionsJson = "[]",
                    fxUsedJson = "{}",
                ),
            )

        whenever(portfolioService.get(userId, portfolioId)).thenReturn(testPortfolio)
        whenever(valuationRepository.findAllByPortfolioIdAndAsOfBetween(portfolioId, from, to))
            .thenReturn(valuations)

        val result = service.history(userId, portfolioId, from, to)

        assertEquals(valuations, result)
        assertEquals(3, result.size)
        verify(portfolioService).get(userId, portfolioId)
        verify(valuationRepository).findAllByPortfolioIdAndAsOfBetween(portfolioId, from, to)
    }

    @Test
    fun `should return empty history when no valuations in range`() {
        val from = OffsetDateTime.now().minusDays(7)
        val to = OffsetDateTime.now()

        whenever(portfolioService.get(userId, portfolioId)).thenReturn(testPortfolio)
        whenever(valuationRepository.findAllByPortfolioIdAndAsOfBetween(portfolioId, from, to))
            .thenReturn(emptyList())

        val result = service.history(userId, portfolioId, from, to)

        assertEquals(emptyList<ValuationEntity>(), result)
    }

    @Test
    fun `should convert positions to PositionInput correctly`() {
        val asOf = OffsetDateTime.now()
        val positions =
            listOf(
                PositionEntity(
                    id = UUID.randomUUID(),
                    portfolioId = portfolioId,
                    accountId = accountId,
                    symbolId = "AAPL",
                    qty = BigDecimal("100"),
                    avgCost = BigDecimal("150.00"),
                    currency = "USD",
                ),
            )

        val valuationResult =
            ValuationResult(
                nav = BigDecimal("15000.00"),
                positions = emptyList(),
                fxUsed = emptyMap(),
            )

        val savedValuation =
            ValuationEntity(
                id = UUID.randomUUID(),
                portfolioId = portfolioId,
                asOf = asOf,
                totalsJson = "{}",
                positionsJson = "[]",
                fxUsedJson = "{}",
            )

        whenever(portfolioService.get(userId, portfolioId)).thenReturn(testPortfolio)
        whenever(positionRepository.findAllByPortfolioId(portfolioId)).thenReturn(positions)
        whenever(calculator.compute(any<List<PositionInput>>(), any()))
            .thenReturn(valuationResult)
        whenever(mapper.writeValueAsString(any())).thenReturn("{}")
        whenever(valuationRepository.save(any<ValuationEntity>())).thenReturn(savedValuation)

        service.runValuation(userId, portfolioId, asOf)

        verify(calculator).compute(
            any<List<PositionInput>>(),
            any(),
        )
    }
}
