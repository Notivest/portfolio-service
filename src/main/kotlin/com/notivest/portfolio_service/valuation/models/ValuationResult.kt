package com.notivest.portfolio_service.valuation.models

import java.math.BigDecimal

data class ValuationResult(
    val positions: List<PositionValuation>,
    val nav: BigDecimal,
    val fxUsed: Map<String, BigDecimal>
)
