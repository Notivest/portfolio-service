package com.notivest.portfolio_service.service.interfaces

import com.notivest.portfolio_service.models.ValuationEntity
import java.time.OffsetDateTime
import java.util.UUID

interface ValuationService {
    fun runValuation(userId: String, portfolioId: UUID, asOf: OffsetDateTime = OffsetDateTime.now()): ValuationEntity
    fun latest(userId: String, portfolioId: UUID): ValuationEntity?
    fun history(userId: String, portfolioId: UUID, from: OffsetDateTime, to: OffsetDateTime): List<ValuationEntity>
}