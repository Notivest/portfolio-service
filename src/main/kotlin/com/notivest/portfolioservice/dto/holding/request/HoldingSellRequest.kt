package com.notivest.portfolioservice.dto.holding.request

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.math.BigDecimal

data class HoldingSellRequest(
    @field:NotBlank(message = "symbol must not be blank")
    @field:Pattern(
        regexp = "^[A-Za-z0-9.\\-]{1,15}$",
        message = "symbol must be 1-15 characters and contain only letters, numbers, '.' or '-'",
    )
    val symbol: String,
    @field:DecimalMin(value = "0.00000001", message = "quantity must be greater than 0")
    val quantity: BigDecimal,
    @field:DecimalMin(value = "0.00000001", message = "price must be greater than 0")
    val price: BigDecimal,
    @field:Size(max = 200, message = "note must be at most 200 characters")
    val note: String? = null,
)
