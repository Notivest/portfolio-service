package com.notivest.portfolioservice.dto.holding

import com.notivest.portfolioservice.dto.holding.request.HoldingCreateRequest
import com.notivest.portfolioservice.dto.holding.request.HoldingUpdateRequest
import com.notivest.portfolioservice.dto.holding.response.HoldingResponse
import com.notivest.portfolioservice.models.HoldingEntity
import com.notivest.portfolioservice.models.portfolio.PortfolioEntity

fun HoldingEntity.toResponse(): HoldingResponse =
    HoldingResponse(
        id = requireNotNull(id) { "HoldingEntity.id must not be null" },
        portfolioId = requireNotNull(portfolio.id) { "HoldingEntity.portfolio.id must not be null" },
        symbol = symbol,
        quantity = quantity,
        avgCost = avgCost,
        createdAt = requireNotNull(createdAt) { "HoldingEntity.createdAt must not be null" },
        updatedAt = requireNotNull(updatedAt) { "HoldingEntity.updatedAt must not be null" },
    )

/** Create DTO -> New Entity (portfolio supplied by caller; no repo access) */
fun HoldingCreateRequest.toEntity(portfolio: PortfolioEntity): HoldingEntity =
    HoldingEntity(
        portfolio = portfolio,
        symbol = symbol,
        quantity = quantity,
        avgCost = avgCost,
        note = note,
    )

/** Update DTO -> apply changes to existing entity (only non-null fields) */
fun HoldingUpdateRequest.applyTo(entity: HoldingEntity): HoldingEntity {
    if (this.quantity != null) {
        entity.quantity = this.quantity
    }
    if (this.avgCost != null) {
        entity.avgCost = this.avgCost
    }
    if (this.note != null) {
        entity.note = this.note
    }
    return entity
}
