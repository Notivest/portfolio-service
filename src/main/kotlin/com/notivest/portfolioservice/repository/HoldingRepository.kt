package com.notivest.portfolioservice.repository

import com.notivest.portfolioservice.models.HoldingEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

@Repository
interface HoldingRepository : JpaRepository<HoldingEntity, UUID> {
    fun findAllByPortfolioId(
        portfolioId: UUID,
        pageable: Pageable,
    ): Page<HoldingEntity>

    fun findAllByPortfolioIdAndSymbolContainingIgnoreCase(
        portfolioId: UUID,
        symbol: String,
        pageable: Pageable,
    ): Page<HoldingEntity>

    fun findByIdAndPortfolioId(
        id: UUID,
        portfolioId: UUID,
    ): Optional<HoldingEntity>

    fun findByPortfolioIdAndSymbolIgnoreCase(
        portfolioId: UUID,
        symbol: String,
    ): Optional<HoldingEntity>

    @Query(
        """
        select h from HoldingEntity h
        join fetch h.portfolio p
        where p.userId = :userId
          and p.deletedAt is null
        """,
    )
    fun findByUserId(userId: UUID): List<HoldingEntity>

    @Query("select distinct h.symbol from HoldingEntity h where h.portfolio.id = :portfolioId")
    fun symbolsByPortfolioId(portfolioId: UUID): Set<String>

    @Query(
        """
        select h from HoldingEntity h
        join fetch h.portfolio p
        where p.userId = :userId
          and p.deletedAt is null
          and upper(h.symbol) in :symbols
        """,
    )
    fun findByUserIdAndSymbols(
        userId: UUID,
        symbols: Collection<String>,
    ): List<HoldingEntity>
}
