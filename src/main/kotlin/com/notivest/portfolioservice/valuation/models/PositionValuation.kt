package com.notivest.portfolioservice.valuation.models

import java.math.BigDecimal

data class PositionValuation(
    val symbolId: String,
    val qty: BigDecimal,
    val avgCost: BigDecimal,
    val lastPrice: BigDecimal,
    val currency: String,
    val fxToBase: BigDecimal,
    val mtmBase: BigDecimal,
    val costBase: BigDecimal,
    val unrealizedPnL: BigDecimal,
)
