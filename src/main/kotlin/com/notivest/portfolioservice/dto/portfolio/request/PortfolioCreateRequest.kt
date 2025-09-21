package com.notivest.portfolioservice.dto.portfolio.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class PortfolioCreateRequest(
    @field:NotBlank(message = "name must not be blank")
    @field:Size(min = 3, max = 80, message = "name must be between 3 and 80 characters")
    val name: String,

    @field:NotBlank(message = "baseCurrency must not be blank")
    @field:Pattern(
        regexp = "^[A-Z]{3}$",
        message = "baseCurrency must be a 3-letter uppercase ISO 4217 code"
    )
    val baseCurrency: String,
)