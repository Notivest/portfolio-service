package com.notivest.portfolio_service.repository

import com.notivest.portfolio_service.models.AccountEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface AccountRepository : JpaRepository<AccountEntity, UUID> {
    fun findAllByPortfolioId(portfolioId: UUID): List<AccountEntity>
}