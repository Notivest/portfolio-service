package com.notivest.portfolioservice.dto.holding.request

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.math.BigDecimal

data class HoldingCreateRequest(
    @field:NotBlank(message = "symbol must not be blank")
    @field:Pattern(
        regexp = "^[A-Z0-9.\\-]{1,15}$",
        message = "symbol must match ^[A-Z0-9.\\-]{1,15}$"
    )
    val symbol: String,

    @field:DecimalMin(value = "0", message = "quantity must be greater than or equal to 0")
    val quantity: BigDecimal? = null,

    @field:DecimalMin(value = "0", message = "avgCost must be greater than or equal to 0")
    val avgCost: BigDecimal? = null,

    @field:Size(max = 200, message = "note must be at most 200 characters")
    val note: String? = null,
)