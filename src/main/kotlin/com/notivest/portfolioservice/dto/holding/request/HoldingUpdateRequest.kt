package com.notivest.portfolioservice.dto.holding.request

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Size
import java.math.BigDecimal

data class HoldingUpdateRequest(
    @field:DecimalMin(value = "0", message = "quantity must be greater than or equal to 0")
    val quantity: BigDecimal? = null,

    @field:DecimalMin(value = "0", message = "avgCost must be greater than or equal to 0")
    val avgCost: BigDecimal? = null,

    @field:Size(max = 200, message = "note must be at most 200 characters")
    val note: String? = null,
)