package com.notivest.portfolioservice.dto.portfolio.response

import com.fasterxml.jackson.annotation.JsonInclude
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@JsonInclude(JsonInclude.Include.NON_NULL)
data class PortfolioWithHoldingsResponse(
    val id: UUID,
    val name: String,
    val baseCurrency: String,
    val holdings: List<PortfolioHoldingResponse>? = null,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class PortfolioHoldingResponse(
    val symbol: String,
    val quantity: BigDecimal?,
    val avgCost: BigDecimal?,
    val marketValue: BigDecimal? = null,
    val asOf: Instant? = null,
)
