package com.notivest.portfolioservice.repository

import com.notivest.portfolioservice.models.HoldingMovementEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface HoldingMovementRepository : JpaRepository<HoldingMovementEntity, UUID> {
    fun findAllByPortfolioId(
        portfolioId: UUID,
        pageable: Pageable,
    ): Page<HoldingMovementEntity>

    fun findAllByPortfolioIdAndSymbolIgnoreCase(
        portfolioId: UUID,
        symbol: String,
        pageable: Pageable,
    ): Page<HoldingMovementEntity>
}
