package com.notivest.portfolioservice.dto.portfolio

import com.notivest.portfolioservice.dto.portfolio.request.PortfolioCreateRequest
import com.notivest.portfolioservice.dto.portfolio.request.PortfolioUpdateRequest
import com.notivest.portfolioservice.dto.portfolio.response.PortfolioResponse
import com.notivest.portfolioservice.models.portfolio.PortfolioEntity
import java.util.UUID


fun PortfolioEntity.toResponse(): PortfolioResponse =
    PortfolioResponse(
        id = requireNotNull(id) { "PortfolioEntity.id must not be null" },
        userId = userId,
        name = name,
        baseCurrency = baseCurrency,
        status = status,
        createdAt = requireNotNull(createdAt) { "PortfolioEntity.createdAt must not be null" },
        updatedAt = requireNotNull(updatedAt) { "PortfolioEntity.updatedAt must not be null" },
    )

/** Create DTO -> New Entity (userId provided by caller; no repo access) */
fun PortfolioCreateRequest.toEntity(userId: UUID): PortfolioEntity =
    PortfolioEntity(
        userId = userId,
        name = name,
        baseCurrency = baseCurrency,
        // status defaults to ACTIVE in entity; no audit fields set here
    )

/** Update DTO -> apply changes to existing entity (only non-null fields) */
fun PortfolioUpdateRequest.applyTo(entity: PortfolioEntity): PortfolioEntity {
    if (this.name != null) {
        entity.name = this.name
    }
    if (this.baseCurrency != null) {
        entity.baseCurrency = this.baseCurrency
    }
    // status & audit fields untouched
    return entity
}