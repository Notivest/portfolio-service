package com.notivest.portfolioservice.valuation.models

import java.math.BigDecimal

data class PositionInput(
    val symbolId: String,
    val qty: BigDecimal,
    val avgCost: BigDecimal,
    val currency: String,
)
