package com.notivest.portfolioservice.dto.portfolio.response

import com.fasterxml.jackson.annotation.JsonInclude
import java.math.BigDecimal
import java.util.UUID

@JsonInclude(JsonInclude.Include.NON_NULL)
data class PortfolioWithHoldingsResponse(
    val id: UUID,
    val name: String,
    val baseCurrency: String,
    val holdings: List<PortfolioHoldingResponse>? = null,
)

data class PortfolioHoldingResponse(
    val symbol: String,
    val quantity: BigDecimal?,
    val avgCost: BigDecimal?,
    val marketValue: BigDecimal? = null,
)
