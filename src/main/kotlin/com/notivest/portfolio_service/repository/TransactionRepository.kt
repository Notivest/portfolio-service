package com.notivest.portfolio_service.repository

import com.notivest.portfolio_service.models.TransactionEntity
import org.springframework.data.domain.Page
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.domain.Pageable
import java.time.LocalDate
import java.util.UUID

interface TransactionRepository : JpaRepository<TransactionEntity, UUID> {
    fun findAllByPortfolioId(portfolioId: UUID): List<TransactionEntity>
    fun findAllByPortfolioIdAndTradeDateBetween(
        portfolioId: UUID, from: LocalDate, to: LocalDate, pageable: Pageable
    ): Page<TransactionEntity>
    fun existsByIdempotencyKey(idempotencyKey: String): Boolean
}