package com.notivest.portfolioservice.dto.holding.request

import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.util.UUID

data class SearchHoldingsRequest(
    @field:NotNull(message = "userId must be provided")
    val userId: UUID?,
    @field:NotEmpty(message = "symbols must contain between 1 and 20 entries")
    @field:Size(min = 1, max = 20, message = "symbols must contain between 1 and 20 entries")
    val symbols: List<String> = emptyList(),
)
