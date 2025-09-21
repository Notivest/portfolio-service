package com.notivest.portfolioservice.dto.portfolio.response

import com.notivest.portfolioservice.models.portfolio.PortfolioStatus
import java.time.Instant
import java.util.UUID

data class PortfolioResponse(
    val id: UUID,
    val userId: UUID,
    val name: String,
    val baseCurrency: String,
    val status: PortfolioStatus,
    val createdAt: Instant,
    val updatedAt: Instant,
)