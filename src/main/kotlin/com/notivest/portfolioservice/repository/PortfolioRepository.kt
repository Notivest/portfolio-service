package com.notivest.portfolioservice.repository

import com.notivest.portfolioservice.models.PortfolioEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface PortfolioRepository : JpaRepository<PortfolioEntity, UUID> {
    fun findAllByUserId(
        userId: String,
        pageable: Pageable,
    ): Page<PortfolioEntity>
}
