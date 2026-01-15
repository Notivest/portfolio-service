package com.notivest.portfolioservice.dto.holding.response

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class HoldingResponse(
    val id: UUID,
    val portfolioId: UUID,
    val symbol: String,
    val quantity: BigDecimal?,
    val avgCost: BigDecimal?,
    val asOf: Instant,
    val bookValue: BigDecimal,
    val note: String? = null,
    val createdAt: Instant,
    val updatedAt: Instant,
)
