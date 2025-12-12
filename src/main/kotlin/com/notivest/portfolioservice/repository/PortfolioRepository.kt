package com.notivest.portfolioservice.repository

import com.notivest.portfolioservice.models.portfolio.PortfolioEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

@Repository
interface PortfolioRepository : JpaRepository<PortfolioEntity, UUID> {
    fun findAllByUserIdAndDeletedAtIsNull(
        userId: UUID,
        pageable: Pageable,
    ): Page<PortfolioEntity>

    fun findAllByUserIdAndDeletedAtIsNull(userId: UUID): List<PortfolioEntity>

    fun findByIdAndUserIdAndDeletedAtIsNull(
        id: UUID,
        userId: UUID,
    ): Optional<PortfolioEntity>
}
