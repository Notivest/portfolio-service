package com.notivest.portfolioservice.service.interfaces

import com.notivest.portfolioservice.models.PositionEntity
import java.util.UUID

interface PositionService {
    fun recomputeFor(
        userId: String,
        portfolioId: UUID,
        accountId: UUID,
        symbolId: String,
    ): PositionEntity

    fun list(
        userId: String,
        portfolioId: UUID,
    ): List<PositionEntity>

    fun getOne(
        userId: String,
        portfolioId: UUID,
        symbolId: String,
    ): PositionEntity?
}
