package com.notivest.portfolioservice.dto.holding.response

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class HoldingSearchResponse(
    val portfolioId: UUID,
    val portfolioName: String,
    val symbol: String,
    val quantity: BigDecimal?,
    val avgCost: BigDecimal?,
    val asOf: Instant,
    val bookValue: BigDecimal,
    val updatedAt: Instant,
)
