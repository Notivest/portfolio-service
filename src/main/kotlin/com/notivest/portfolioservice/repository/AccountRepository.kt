package com.notivest.portfolioservice.repository

import com.notivest.portfolioservice.models.AccountEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface AccountRepository : JpaRepository<AccountEntity, UUID> {
    fun findAllByPortfolioId(portfolioId: UUID): List<AccountEntity>
}
