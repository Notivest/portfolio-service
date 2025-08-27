package com.notivest.portfolio_service.repository

import com.notivest.portfolio_service.models.PositionEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface PositionRepository : JpaRepository<PositionEntity, UUID> {
    fun findAllByPortfolioId(portfolioId: UUID): List<PositionEntity>
    fun findByPortfolioIdAndSymbolId(portfolioId: UUID, symbolId: String): PositionEntity?
}