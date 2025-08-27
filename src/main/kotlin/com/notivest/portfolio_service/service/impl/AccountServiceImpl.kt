package com.notivest.portfolio_service.service.impl

import com.notivest.portfolio_service.models.AccountEntity
import com.notivest.portfolio_service.repository.AccountRepository
import com.notivest.portfolio_service.service.interfaces.AccountService
import com.notivest.portfolio_service.service.interfaces.PortfolioService
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.UUID

@Service
class AccountServiceImpl(
    private val accountRepository: AccountRepository,
    private val portfolioService : PortfolioService
) : AccountService{
    override fun create(
        userId: String,
        portfolioId: UUID,
        name: String,
        currency: String
    ): AccountEntity {
        portfolioService.get(userId, portfolioId)
        val entity = AccountEntity(portfolioId = portfolioId, name = name, currency = currency.uppercase())
        return accountRepository.save(entity)
    }

    override fun get(
        userId: String,
        portfolioId: UUID,
        accountId: UUID
    ): AccountEntity {
        portfolioService.get(userId, portfolioId)
        val acc = accountRepository.findById(accountId).orElseThrow { NoSuchElementException("Account not found")}
        require(acc.portfolioId == portfolioId) { "Portfolio is not from that account" }
        return acc
    }

    override fun list(
        userId: String,
        portfolioId: UUID
    ): List<AccountEntity> {
        portfolioService.get(userId, portfolioId)
        return accountRepository.findAllByPortfolioId(portfolioId)
    }

    override fun update(
        userId: String,
        portfolioId: UUID,
        accountId: UUID,
        name: String?,
        currency: String?
    ): AccountEntity {
        val acc = get(userId, portfolioId, accountId)
        name?.let { acc.name = it }
        currency?.let { acc.currency = it.uppercase() }
        acc.updatedAt = OffsetDateTime.now()
        return accountRepository.save(acc)
    }

    override fun delete(userId: String, portfolioId: UUID, accountId: UUID) {
        val acc = get(userId, portfolioId, accountId)
        accountRepository.delete(acc)
    }
}