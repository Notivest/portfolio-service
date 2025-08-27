package com.notivest.portfolio_service.repository

import com.notivest.portfolio_service.models.ValuationEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime
import java.util.UUID

interface ValuationRepository : JpaRepository<ValuationEntity, UUID> {
    fun findTop1ByPortfolioIdOrderByAsOfDesc(portfolioId: UUID): ValuationEntity?
    fun findAllByPortfolioIdAndAsOfBetween(
        portfolioId: UUID,
        from: OffsetDateTime,
        to: OffsetDateTime
    ): List<ValuationEntity>
}