package com.notivest.portfolio_service.controller.dto

import com.notivest.portfolio_service.models.PositionEntity
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.UUID

data class PositionResponse(
    val id: UUID,
    val portfolioId: UUID,
    val accountId: UUID,
    val symbolId: String,
    val qty: BigDecimal,
    val avgCost: BigDecimal,
    val currency: String,
    val updatedAt: OffsetDateTime?
) {
    companion object {
        fun fromEntity(e: PositionEntity) = PositionResponse(
            id = e.id,
            portfolioId = e.portfolioId,
            accountId = e.accountId,
            symbolId = e.symbolId,
            qty = e.qty,
            avgCost = e.avgCost,
            currency = e.currency,
            updatedAt = e.updatedAt
        )
    }
}

data class RecomputePositionRequest(
    val accountId: UUID,
    val symbolId: String
)