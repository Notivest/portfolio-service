package com.notivest.portfolioservice.service.impl

import com.notivest.portfolioservice.controller.dto.TransactionDTO
import com.notivest.portfolioservice.controller.exception.ConflictException
import com.notivest.portfolioservice.models.AccountEntity
import com.notivest.portfolioservice.models.PortfolioEntity
import com.notivest.portfolioservice.models.TransactionEntity
import com.notivest.portfolioservice.models.enums.TransactionType
import com.notivest.portfolioservice.repository.TransactionRepository
import com.notivest.portfolioservice.service.interfaces.AccountService
import com.notivest.portfolioservice.service.interfaces.PortfolioService
import com.notivest.portfolioservice.service.interfaces.PositionService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.time.LocalDate
import java.util.Optional
import java.util.UUID

class TransactionalServiceImplTest {
    private val transactionRepository: TransactionRepository = mock()
    private val accountService: AccountService = mock()
    private val portfolioService: PortfolioService = mock()
    private val positionService: PositionService = mock()

    private val service =
        TransactionalServiceImpl(
            transactionRepository,
            accountService,
            portfolioService,
            positionService,
        )

    private val userId = "testuser"
    private val portfolioId = UUID.randomUUID()
    private val accountId = UUID.randomUUID()

    @Test
    fun `should create BUY transaction successfully`() {
        val portfolio = PortfolioEntity(userId, "Test Portfolio", "USD")
        val account = AccountEntity(accountId, portfolioId, "Test Account", "USD")
        val transactionDto =
            TransactionDTO(
                accountId = accountId,
                type = TransactionType.BUY,
                symbolId = "AAPL",
                qty = BigDecimal("10"),
                price = BigDecimal("150.0"),
                currency = "USD",
                tradeDate = LocalDate.now(),
            )

        val savedTransaction =
            TransactionEntity(
                id = UUID.randomUUID(),
                portfolioId = portfolioId,
                accountId = accountId,
                symbolId = "AAPL",
                type = TransactionType.BUY,
                qty = BigDecimal("10"),
                price = BigDecimal("150.0"),
                currency = "USD",
                tradeDate = LocalDate.now(),
                fees = BigDecimal.ZERO,
                taxes = BigDecimal.ZERO,
            )

        whenever(portfolioService.get(userId, portfolioId)).thenReturn(portfolio)
        whenever(accountService.get(userId, portfolioId, accountId)).thenReturn(account)
        whenever(transactionRepository.existsByIdempotencyKey(any())).thenReturn(false)
        whenever(transactionRepository.save(any<TransactionEntity>())).thenReturn(savedTransaction)

        val result = service.post(userId, portfolioId, transactionDto, "test-key")

        assertNotNull(result)
        assertEquals("AAPL", result.symbolId)
        assertEquals(TransactionType.BUY, result.type)
        verify(positionService).recomputeFor(userId, portfolioId, accountId, "AAPL")
    }

    @Test
    fun `should throw ConflictException for duplicate idempotency key`() {
        val transactionDto =
            TransactionDTO(
                accountId = accountId,
                type = TransactionType.BUY,
                symbolId = "AAPL",
                qty = BigDecimal("10"),
                price = BigDecimal("150.0"),
                currency = "USD",
                tradeDate = LocalDate.now(),
            )

        whenever(portfolioService.get(userId, portfolioId)).thenReturn(mock())
        whenever(accountService.get(userId, portfolioId, accountId)).thenReturn(mock())
        whenever(transactionRepository.existsByIdempotencyKey("duplicate-key")).thenReturn(true)

        assertThrows<ConflictException> {
            service.post(userId, portfolioId, transactionDto, "duplicate-key")
        }

        verify(transactionRepository, never()).save(any<TransactionEntity>())
    }

    @Test
    fun `should throw IllegalArgumentException for invalid currency`() {
        val transactionDto =
            TransactionDTO(
                accountId = accountId,
                type = TransactionType.BUY,
                symbolId = "AAPL",
                qty = BigDecimal("10"),
                price = BigDecimal("150.0"),
                currency = "INVALID",
                tradeDate = LocalDate.now(),
            )

        whenever(portfolioService.get(userId, portfolioId)).thenReturn(mock())
        whenever(accountService.get(userId, portfolioId, accountId)).thenReturn(mock())
        whenever(transactionRepository.existsByIdempotencyKey(any())).thenReturn(false)

        assertThrows<IllegalArgumentException> {
            service.post(userId, portfolioId, transactionDto, null)
        }
    }

    @Test
    fun `should get transaction successfully`() {
        val transactionId = UUID.randomUUID()
        val transaction =
            TransactionEntity(
                id = transactionId,
                portfolioId = portfolioId,
                accountId = accountId,
                type = TransactionType.BUY,
                currency = "USD",
                tradeDate = LocalDate.now(),
                fees = BigDecimal.ZERO,
                taxes = BigDecimal.ZERO,
            )

        whenever(portfolioService.get(userId, portfolioId)).thenReturn(mock())
        whenever(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction))

        val result = service.get(userId, portfolioId, transactionId)

        assertEquals(transactionId, result.id)
        assertEquals(portfolioId, result.portfolioId)
    }

    @Test
    fun `should throw NoSuchElementException for non-existent transaction`() {
        val transactionId = UUID.randomUUID()

        whenever(portfolioService.get(userId, portfolioId)).thenReturn(mock())
        whenever(transactionRepository.findById(transactionId)).thenReturn(Optional.empty())

        assertThrows<NoSuchElementException> {
            service.get(userId, portfolioId, transactionId)
        }
    }

    @Test
    fun `should create SELL transaction successfully`() {
        val portfolio = PortfolioEntity(userId, "Test Portfolio", "USD")
        val account = AccountEntity(accountId, portfolioId, "Test Account", "USD")
        val transactionDto =
            TransactionDTO(
                accountId = accountId,
                type = TransactionType.SELL,
                symbolId = "AAPL",
                qty = BigDecimal("50"),
                price = BigDecimal("160.00"),
                currency = "USD",
                tradeDate = LocalDate.now(),
            )
        val savedTransaction =
            TransactionEntity(
                id = UUID.randomUUID(),
                portfolioId = portfolioId,
                accountId = accountId,
                type = TransactionType.SELL,
                symbolId = "AAPL",
                qty = BigDecimal("50"),
                price = BigDecimal("160.00"),
                currency = "USD",
                tradeDate = LocalDate.now(),
                fees = BigDecimal.ZERO,
                taxes = BigDecimal.ZERO,
            )

        whenever(portfolioService.get(userId, portfolioId)).thenReturn(portfolio)
        whenever(accountService.get(userId, portfolioId, accountId)).thenReturn(account)
        whenever(transactionRepository.existsByIdempotencyKey(any())).thenReturn(false)
        whenever(transactionRepository.save(any<TransactionEntity>())).thenReturn(savedTransaction)

        val result = service.post(userId, portfolioId, transactionDto, null)

        assertNotNull(result)
        assertEquals(TransactionType.SELL, result.type)
        assertEquals(BigDecimal("50"), result.qty)
        verify(positionService).recomputeFor(userId, portfolioId, accountId, "AAPL")
    }

    @Test
    fun `should create CASH transaction without position recompute`() {
        val portfolio = PortfolioEntity(userId, "Test Portfolio", "USD")
        val account = AccountEntity(accountId, portfolioId, "Test Account", "USD")
        val transactionDto =
            TransactionDTO(
                accountId = accountId,
                type = TransactionType.CASH_IN,
                symbolId = null,
                qty = null,
                price = null,
                currency = "USD",
                tradeDate = LocalDate.now(),
            )
        val savedTransaction =
            TransactionEntity(
                id = UUID.randomUUID(),
                portfolioId = portfolioId,
                accountId = accountId,
                type = TransactionType.CASH_IN,
                symbolId = null,
                qty = null,
                price = null,
                currency = "USD",
                tradeDate = LocalDate.now(),
                fees = BigDecimal.ZERO,
                taxes = BigDecimal.ZERO,
            )

        whenever(portfolioService.get(userId, portfolioId)).thenReturn(portfolio)
        whenever(accountService.get(userId, portfolioId, accountId)).thenReturn(account)
        whenever(transactionRepository.existsByIdempotencyKey(any())).thenReturn(false)
        whenever(transactionRepository.save(any<TransactionEntity>())).thenReturn(savedTransaction)

        val result = service.post(userId, portfolioId, transactionDto, null)

        assertNotNull(result)
        assertEquals(TransactionType.CASH_IN, result.type)
        verify(transactionRepository).save(any<TransactionEntity>())
        verify(positionService, never()).recomputeFor(any(), any(), any(), any())
    }
}
