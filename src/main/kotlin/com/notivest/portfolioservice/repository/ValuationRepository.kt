package com.notivest.portfolioservice.repository

import com.notivest.portfolioservice.models.ValuationEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.time.OffsetDateTime
import java.util.UUID

interface ValuationRepository : JpaRepository<ValuationEntity, UUID> {
    fun findTop1ByPortfolioIdOrderByAsOfDesc(portfolioId: UUID): ValuationEntity?

    fun findAllByPortfolioIdAndAsOfBetween(
        portfolioId: UUID,
        from: OffsetDateTime,
        to: OffsetDateTime,
    ): List<ValuationEntity>
}
