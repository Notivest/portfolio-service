package com.notivest.portfolioservice.service.impl

import com.notivest.portfolioservice.models.AccountEntity
import com.notivest.portfolioservice.models.PortfolioEntity
import com.notivest.portfolioservice.repository.AccountRepository
import com.notivest.portfolioservice.service.interfaces.PortfolioService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Optional
import java.util.UUID

class AccountServiceImplTest {
    private val accountRepository: AccountRepository = mock()
    private val portfolioService: PortfolioService = mock()
    private val service = AccountServiceImpl(accountRepository, portfolioService)

    private val userId = "testuser"
    private val portfolioId = UUID.randomUUID()
    private val accountId = UUID.randomUUID()
    private val testPortfolio =
        PortfolioEntity(
            userId = userId,
            name = "Test Portfolio",
            baseCurrency = "USD",
        )

    @Test
    fun `should create account successfully`() {
        val name = "Test Account"
        val currency = "usd"
        val expectedEntity =
            AccountEntity(
                id = accountId,
                portfolioId = portfolioId,
                name = name,
                currency = "USD",
            )

        whenever(portfolioService.get(userId, portfolioId)).thenReturn(testPortfolio)
        whenever(accountRepository.save(any<AccountEntity>())).thenReturn(expectedEntity)

        val result = service.create(userId, portfolioId, name, currency)

        assertEquals(expectedEntity, result)
        assertEquals("USD", result.currency) // Should be uppercase
        verify(portfolioService).get(userId, portfolioId)
        verify(accountRepository).save(any<AccountEntity>())
    }

    @Test
    fun `should throw exception when portfolio not found during create`() {
        val name = "Test Account"
        val currency = "USD"

        whenever(portfolioService.get(userId, portfolioId))
            .thenThrow(NoSuchElementException("Portfolio not found"))

        assertThrows<NoSuchElementException> {
            service.create(userId, portfolioId, name, currency)
        }

        verify(portfolioService).get(userId, portfolioId)
        verify(accountRepository, never()).save(any<AccountEntity>())
    }

    @Test
    fun `should get account by id`() {
        val expectedEntity =
            AccountEntity(
                id = accountId,
                portfolioId = portfolioId,
                name = "Test Account",
                currency = "USD",
            )

        whenever(portfolioService.get(userId, portfolioId)).thenReturn(testPortfolio)
        whenever(accountRepository.findById(accountId)).thenReturn(Optional.of(expectedEntity))

        val result = service.get(userId, portfolioId, accountId)

        assertEquals(expectedEntity, result)
        verify(portfolioService).get(userId, portfolioId)
        verify(accountRepository).findById(accountId)
    }

    @Test
    fun `should throw exception when account not found`() {
        whenever(portfolioService.get(userId, portfolioId)).thenReturn(testPortfolio)
        whenever(accountRepository.findById(accountId)).thenReturn(Optional.empty())

        assertThrows<NoSuchElementException> {
            service.get(userId, portfolioId, accountId)
        }
    }

    @Test
    fun `should throw exception when account belongs to different portfolio`() {
        val differentPortfolioId = UUID.randomUUID()
        val accountEntity =
            AccountEntity(
                id = accountId,
                portfolioId = differentPortfolioId,
                name = "Test Account",
                currency = "USD",
            )

        whenever(portfolioService.get(userId, portfolioId)).thenReturn(testPortfolio)
        whenever(accountRepository.findById(accountId)).thenReturn(Optional.of(accountEntity))

        assertThrows<IllegalArgumentException> {
            service.get(userId, portfolioId, accountId)
        }
    }

    @Test
    fun `should list accounts for portfolio`() {
        val accounts =
            listOf(
                AccountEntity(
                    id = UUID.randomUUID(),
                    portfolioId = portfolioId,
                    name = "Account 1",
                    currency = "USD",
                ),
                AccountEntity(
                    id = UUID.randomUUID(),
                    portfolioId = portfolioId,
                    name = "Account 2",
                    currency = "EUR",
                ),
            )

        whenever(portfolioService.get(userId, portfolioId)).thenReturn(testPortfolio)
        whenever(accountRepository.findAllByPortfolioId(portfolioId)).thenReturn(accounts)

        val result = service.list(userId, portfolioId)

        assertEquals(accounts, result)
        assertEquals(2, result.size)
        verify(portfolioService).get(userId, portfolioId)
        verify(accountRepository).findAllByPortfolioId(portfolioId)
    }

    @Test
    fun `should update account name and currency`() {
        val originalEntity =
            AccountEntity(
                id = accountId,
                portfolioId = portfolioId,
                name = "Original Name",
                currency = "USD",
            )

        whenever(portfolioService.get(userId, portfolioId)).thenReturn(testPortfolio)
        whenever(accountRepository.findById(accountId)).thenReturn(Optional.of(originalEntity))
        whenever(accountRepository.save(any<AccountEntity>())).thenReturn(originalEntity)

        val result = service.update(userId, portfolioId, accountId, "New Name", "eur")

        assertEquals("New Name", originalEntity.name)
        assertEquals("EUR", originalEntity.currency)
        assertNotNull(originalEntity.updatedAt)
        verify(accountRepository).save(originalEntity)
    }

    @Test
    fun `should update only name when currency is null`() {
        val originalEntity =
            AccountEntity(
                id = accountId,
                portfolioId = portfolioId,
                name = "Original Name",
                currency = "USD",
            )

        whenever(portfolioService.get(userId, portfolioId)).thenReturn(testPortfolio)
        whenever(accountRepository.findById(accountId)).thenReturn(Optional.of(originalEntity))
        whenever(accountRepository.save(any<AccountEntity>())).thenReturn(originalEntity)

        service.update(userId, portfolioId, accountId, "New Name", null)

        assertEquals("New Name", originalEntity.name)
        assertEquals("USD", originalEntity.currency) // Should remain unchanged
    }

    @Test
    fun `should delete account`() {
        val entity =
            AccountEntity(
                id = accountId,
                portfolioId = portfolioId,
                name = "Test Account",
                currency = "USD",
            )

        whenever(portfolioService.get(userId, portfolioId)).thenReturn(testPortfolio)
        whenever(accountRepository.findById(accountId)).thenReturn(Optional.of(entity))

        service.delete(userId, portfolioId, accountId)

        verify(accountRepository).delete(entity)
    }
}
