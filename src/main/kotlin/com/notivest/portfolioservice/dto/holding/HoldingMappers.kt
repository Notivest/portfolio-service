package com.notivest.portfolioservice.dto.holding

import com.notivest.portfolioservice.dto.holding.request.HoldingCreateRequest
import com.notivest.portfolioservice.dto.holding.request.HoldingUpdateRequest
import com.notivest.portfolioservice.dto.holding.response.HoldingResponse
import com.notivest.portfolioservice.dto.portfolio.response.PortfolioHoldingResponse
import com.notivest.portfolioservice.models.HoldingEntity
import com.notivest.portfolioservice.models.portfolio.PortfolioEntity
import java.math.BigDecimal

fun HoldingEntity.toResponse(): HoldingResponse {
    val qty = quantity
    val cost = avgCost
    val bookValue =
        if (qty != null && cost != null) {
            qty * cost
        } else {
            BigDecimal.ZERO
        }

    return HoldingResponse(
        id = requireNotNull(id) { "HoldingEntity.id must not be null" },
        portfolioId = requireNotNull(portfolio.id) { "HoldingEntity.portfolio.id must not be null" },
        symbol = symbol,
        quantity = quantity,
        avgCost = avgCost,
        asOf = requireNotNull(updatedAt) { "HoldingEntity.updatedAt must not be null" },
        bookValue = bookValue,
        note = note,
        createdAt = requireNotNull(createdAt) { "HoldingEntity.createdAt must not be null" },
        updatedAt = requireNotNull(updatedAt) { "HoldingEntity.updatedAt must not be null" },
    )
}

fun HoldingEntity.toPortfolioHoldingResponse(): PortfolioHoldingResponse =
    run {
        val qty = quantity
        val cost = avgCost
        val bookValue =
            if (qty != null && cost != null) {
                qty * cost
            } else {
                BigDecimal.ZERO
            }

        PortfolioHoldingResponse(
            symbol = symbol,
            quantity = qty,
            avgCost = cost,
            bookValue = bookValue,
            asOf = requireNotNull(updatedAt) { "HoldingEntity.updatedAt must not be null" },
        )
    }

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
