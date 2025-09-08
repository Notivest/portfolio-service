package com.notivest.portfolioservice.controller.dto

import com.notivest.portfolioservice.models.PortfolioEntity
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.util.UUID

data class PortfolioResponse(val id: UUID, val name: String, val baseCurrency: String) {
    companion object {
        fun fromEntity(e: PortfolioEntity) = PortfolioResponse(e.id, e.name, e.baseCurrency)
    }
}

data class CreatePortfolioRequest(
    @field:NotBlank(message = "Portfolio name is required")
    @field:Size(min = 1, max = 100, message = "Portfolio name must be between 1 and 100 characters")
    val name: String,
    @field:NotBlank(message = "Base currency is required")
    @field:Size(min = 3, max = 3, message = "Currency must be exactly 3 characters")
    val baseCurrency: String,
)

data class UpdatePortfolioRequest(
    @field:Size(min = 1, max = 100, message = "Portfolio name must be between 1 and 100 characters")
    val name: String?,
    @field:Size(min = 3, max = 3, message = "Currency must be exactly 3 characters")
    val baseCurrency: String?,
)
