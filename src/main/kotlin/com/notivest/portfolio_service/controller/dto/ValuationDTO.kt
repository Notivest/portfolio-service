package com.notivest.portfolio_service.controller.dto

import com.notivest.portfolio_service.models.ValuationEntity
import java.time.OffsetDateTime
import java.util.UUID

data class RunValuationRequest(
    val asOf: OffsetDateTime? = null
)

data class ValuationResponse(
    val id: UUID,
    val portfolioId: UUID,
    val asOf: OffsetDateTime,
    val totalsJson: String,
    val positionsJson: String,
    val fxUsedJson: String?,
    val createdAt: OffsetDateTime?
) {
    companion object {
        fun fromEntity(e: ValuationEntity) = ValuationResponse(
            id = e.id,
            portfolioId = e.portfolioId,
            asOf = e.asOf,
            totalsJson = e.totalsJson,
            positionsJson = e.positionsJson,
            fxUsedJson = e.fxUsedJson,
            createdAt = e.createdAt
        )
    }
}