package com.notivest.portfolioservice.service.impl

import com.notivest.portfolioservice.models.PortfolioEntity
import com.notivest.portfolioservice.models.PositionEntity
import com.notivest.portfolioservice.models.TransactionEntity
import com.notivest.portfolioservice.models.enums.TransactionType
import com.notivest.portfolioservice.repository.PositionRepository
import com.notivest.portfolioservice.repository.TransactionRepository
import com.notivest.portfolioservice.service.interfaces.PortfolioService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

class PositionServiceImplTest {
    private val portfolioService: PortfolioService = mock()
    private val transactionRepository: TransactionRepository = mock()
    private val positionRepository: PositionRepository = mock()
    private val service = PositionServiceImpl(portfolioService, transactionRepository, positionRepository)

    private val userId = "testuser"
    private val portfolioId = UUID.randomUUID()
    private val accountId = UUID.randomUUID()
    private val symbolId = "AAPL"
    private val testPortfolio =
        PortfolioEntity(
            id = portfolioId,
            userId = userId,
            name = "Test Portfolio",
            baseCurrency = "USD",
        )

    @Test
    fun `should recompute position with single BUY transaction`() {
        val transactions =
            listOf(
                TransactionEntity(
                    id = UUID.randomUUID(),
                    portfolioId = portfolioId,
                    accountId = accountId,
                    type = TransactionType.BUY,
                    symbolId = symbolId,
                    qty = BigDecimal("100"),
                    price = BigDecimal("150.00"),
                    currency = "USD",
                    tradeDate = LocalDate.now(),
                    fees = BigDecimal("5.00"),
                    taxes = BigDecimal("2.00"),
                ),
            )

        val expectedPosition =
            PositionEntity(
                id = UUID.randomUUID(),
                portfolioId = portfolioId,
                accountId = accountId,
                symbolId = symbolId,
                qty = BigDecimal("100"),
                // (150*100 + 5 + 2) / 100
                avgCost = BigDecimal("150.07"),
                currency = "USD",
            )

        whenever(portfolioService.get(userId, portfolioId)).thenReturn(testPortfolio)
        whenever(transactionRepository.findAllByPortfolioId(portfolioId)).thenReturn(transactions)
        whenever(positionRepository.findByPortfolioIdAndSymbolId(portfolioId, symbolId)).thenReturn(null)
        whenever(positionRepository.save(any<PositionEntity>())).thenReturn(expectedPosition)

        val result = service.recomputeFor(userId, portfolioId, accountId, symbolId)

        assertEquals(BigDecimal("100"), result.qty)
        assertEquals(BigDecimal("150.07"), result.avgCost)
        verify(positionRepository).save(any<PositionEntity>())
    }

    @Test
    fun `should recompute position with multiple BUY transactions`() {
        val transactions =
            listOf(
                TransactionEntity(
                    id = UUID.randomUUID(),
                    portfolioId = portfolioId,
                    accountId = accountId,
                    type = TransactionType.BUY,
                    symbolId = symbolId,
                    qty = BigDecimal("100"),
                    price = BigDecimal("100.00"),
                    currency = "USD",
                    tradeDate = LocalDate.now().minusDays(2),
                    fees = BigDecimal.ZERO,
                    taxes = BigDecimal.ZERO,
                ),
                TransactionEntity(
                    id = UUID.randomUUID(),
                    portfolioId = portfolioId,
                    accountId = accountId,
                    type = TransactionType.BUY,
                    symbolId = symbolId,
                    qty = BigDecimal("100"),
                    price = BigDecimal("200.00"),
                    currency = "USD",
                    tradeDate = LocalDate.now().minusDays(1),
                    fees = BigDecimal.ZERO,
                    taxes = BigDecimal.ZERO,
                ),
            )

        val expectedPosition =
            PositionEntity(
                id = UUID.randomUUID(),
                portfolioId = portfolioId,
                accountId = accountId,
                symbolId = symbolId,
                qty = BigDecimal("200"),
                // (100*100 + 200*100) / 200
                avgCost = BigDecimal("150.00"),
                currency = "USD",
            )

        whenever(portfolioService.get(userId, portfolioId)).thenReturn(testPortfolio)
        whenever(transactionRepository.findAllByPortfolioId(portfolioId)).thenReturn(transactions)
        whenever(positionRepository.findByPortfolioIdAndSymbolId(portfolioId, symbolId)).thenReturn(null)
        whenever(positionRepository.save(any<PositionEntity>())).thenReturn(expectedPosition)

        val result = service.recomputeFor(userId, portfolioId, accountId, symbolId)

        assertEquals(BigDecimal("200"), result.qty)
        assertEquals(BigDecimal("150.00"), result.avgCost)
    }

    @Test
    fun `should recompute position with BUY and SELL transactions`() {
        val transactions =
            listOf(
                TransactionEntity(
                    id = UUID.randomUUID(),
                    portfolioId = portfolioId,
                    accountId = accountId,
                    type = TransactionType.BUY,
                    symbolId = symbolId,
                    qty = BigDecimal("100"),
                    price = BigDecimal("150.00"),
                    currency = "USD",
                    tradeDate = LocalDate.now().minusDays(2),
                    fees = BigDecimal.ZERO,
                    taxes = BigDecimal.ZERO,
                ),
                TransactionEntity(
                    id = UUID.randomUUID(),
                    portfolioId = portfolioId,
                    accountId = accountId,
                    type = TransactionType.SELL,
                    symbolId = symbolId,
                    qty = BigDecimal("30"),
                    price = BigDecimal("200.00"),
                    currency = "USD",
                    tradeDate = LocalDate.now().minusDays(1),
                    fees = BigDecimal.ZERO,
                    taxes = BigDecimal.ZERO,
                ),
            )

        val expectedPosition =
            PositionEntity(
                id = UUID.randomUUID(),
                portfolioId = portfolioId,
                accountId = accountId,
                symbolId = symbolId,
                // 100 - 30
                qty = BigDecimal("70"),
                // avgCost remains the same for SELL
                avgCost = BigDecimal("150.00"),
                currency = "USD",
            )

        whenever(portfolioService.get(userId, portfolioId)).thenReturn(testPortfolio)
        whenever(transactionRepository.findAllByPortfolioId(portfolioId)).thenReturn(transactions)
        whenever(positionRepository.findByPortfolioIdAndSymbolId(portfolioId, symbolId)).thenReturn(null)
        whenever(positionRepository.save(any<PositionEntity>())).thenReturn(expectedPosition)

        val result = service.recomputeFor(userId, portfolioId, accountId, symbolId)

        assertEquals(BigDecimal("70"), result.qty)
        assertEquals(BigDecimal("150.00"), result.avgCost)
    }

    @Test
    fun `should update existing position when recomputing`() {
        val existingPosition =
            PositionEntity(
                id = UUID.randomUUID(),
                portfolioId = portfolioId,
                accountId = accountId,
                symbolId = symbolId,
                qty = BigDecimal("50"),
                avgCost = BigDecimal("120.00"),
                currency = "EUR",
            )

        val transactions =
            listOf(
                TransactionEntity(
                    id = UUID.randomUUID(),
                    portfolioId = portfolioId,
                    accountId = accountId,
                    type = TransactionType.BUY,
                    symbolId = symbolId,
                    qty = BigDecimal("100"),
                    price = BigDecimal("150.00"),
                    currency = "USD",
                    tradeDate = LocalDate.now(),
                    fees = BigDecimal.ZERO,
                    taxes = BigDecimal.ZERO,
                ),
            )

        whenever(portfolioService.get(userId, portfolioId)).thenReturn(testPortfolio)
        whenever(transactionRepository.findAllByPortfolioId(portfolioId)).thenReturn(transactions)
        whenever(positionRepository.findByPortfolioIdAndSymbolId(portfolioId, symbolId))
            .thenReturn(existingPosition)
        whenever(positionRepository.save(any<PositionEntity>())).thenAnswer { invocation ->
            invocation.getArgument<PositionEntity>(0)
        }

        val result = service.recomputeFor(userId, portfolioId, accountId, symbolId)

        assertEquals(0, result.qty.compareTo(BigDecimal("100")))
        assertEquals(0, result.avgCost.compareTo(BigDecimal("150.00")))
        assertEquals("EUR", result.currency) // Should keep existing currency
        assertNotNull(result.updatedAt)
        verify(positionRepository).save(any<PositionEntity>())
    }

    @Test
    fun `should list positions for portfolio`() {
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

        whenever(portfolioService.get(userId, portfolioId)).thenReturn(testPortfolio)
        whenever(positionRepository.findAllByPortfolioId(portfolioId)).thenReturn(positions)

        val result = service.list(userId, portfolioId)

        assertEquals(positions, result)
        assertEquals(2, result.size)
        verify(portfolioService).get(userId, portfolioId)
        verify(positionRepository).findAllByPortfolioId(portfolioId)
    }

    @Test
    fun `should get specific position by symbol`() {
        val position =
            PositionEntity(
                id = UUID.randomUUID(),
                portfolioId = portfolioId,
                accountId = accountId,
                symbolId = symbolId,
                qty = BigDecimal("100"),
                avgCost = BigDecimal("150.00"),
                currency = "USD",
            )

        whenever(portfolioService.get(userId, portfolioId)).thenReturn(testPortfolio)
        whenever(positionRepository.findByPortfolioIdAndSymbolId(portfolioId, symbolId))
            .thenReturn(position)

        val result = service.getOne(userId, portfolioId, symbolId)

        assertEquals(position, result)
        verify(portfolioService).get(userId, portfolioId)
        verify(positionRepository).findByPortfolioIdAndSymbolId(portfolioId, symbolId)
    }

    @Test
    fun `should return null when position not found`() {
        whenever(portfolioService.get(userId, portfolioId)).thenReturn(testPortfolio)
        whenever(positionRepository.findByPortfolioIdAndSymbolId(portfolioId, symbolId))
            .thenReturn(null)

        val result = service.getOne(userId, portfolioId, symbolId)

        assertNull(result)
    }

    @Test
    fun `should filter transactions by account and symbol when recomputing`() {
        val otherAccountId = UUID.randomUUID()
        val otherSymbolId = "GOOGL"

        val transactions =
            listOf(
                // This should be included
                TransactionEntity(
                    id = UUID.randomUUID(),
                    portfolioId = portfolioId,
                    accountId = accountId,
                    type = TransactionType.BUY,
                    symbolId = symbolId,
                    qty = BigDecimal("100"),
                    price = BigDecimal("150.00"),
                    currency = "USD",
                    tradeDate = LocalDate.now(),
                    fees = BigDecimal.ZERO,
                    taxes = BigDecimal.ZERO,
                ),
                // This should be excluded (different account)
                TransactionEntity(
                    id = UUID.randomUUID(),
                    portfolioId = portfolioId,
                    accountId = otherAccountId,
                    type = TransactionType.BUY,
                    symbolId = symbolId,
                    qty = BigDecimal("200"),
                    price = BigDecimal("200.00"),
                    currency = "USD",
                    tradeDate = LocalDate.now(),
                    fees = BigDecimal.ZERO,
                    taxes = BigDecimal.ZERO,
                ),
                // This should be excluded (different symbol)
                TransactionEntity(
                    id = UUID.randomUUID(),
                    portfolioId = portfolioId,
                    accountId = accountId,
                    type = TransactionType.BUY,
                    symbolId = otherSymbolId,
                    qty = BigDecimal("300"),
                    price = BigDecimal("300.00"),
                    currency = "USD",
                    tradeDate = LocalDate.now(),
                    fees = BigDecimal.ZERO,
                    taxes = BigDecimal.ZERO,
                ),
            )

        val expectedPosition =
            PositionEntity(
                id = UUID.randomUUID(),
                portfolioId = portfolioId,
                accountId = accountId,
                symbolId = symbolId,
                // Only the first transaction should be processed
                qty = BigDecimal("100"),
                avgCost = BigDecimal("150.00"),
                currency = "USD",
            )

        whenever(portfolioService.get(userId, portfolioId)).thenReturn(testPortfolio)
        whenever(transactionRepository.findAllByPortfolioId(portfolioId)).thenReturn(transactions)
        whenever(positionRepository.findByPortfolioIdAndSymbolId(portfolioId, symbolId)).thenReturn(null)
        whenever(positionRepository.save(any<PositionEntity>())).thenReturn(expectedPosition)

        val result = service.recomputeFor(userId, portfolioId, accountId, symbolId)

        assertEquals(BigDecimal("100"), result.qty)
        assertEquals(BigDecimal("150.00"), result.avgCost)
    }
}
