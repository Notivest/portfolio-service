package com.notivest.portfolioservice.dto.holding.response

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class HoldingSummaryResponse(
    val id: UUID,
    val symbol: String,
    val quantity: BigDecimal,
    val avgCost: BigDecimal,
    val marketValue: BigDecimal,
    val weight: BigDecimal,
    val asOf: Instant,
)
