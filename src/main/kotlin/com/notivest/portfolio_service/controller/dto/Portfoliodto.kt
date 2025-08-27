package com.notivest.portfolio_service.controller.dto

import com.notivest.portfolio_service.models.PortfolioEntity
import java.util.UUID

data class PortfolioResponse(val id: UUID, val name: String, val baseCurrency: String) {
    companion object { fun fromEntity(e: PortfolioEntity) = PortfolioResponse(e.id, e.name, e.baseCurrency) }
}

data class CreatePortfolioRequest(val name: String, val baseCurrency: String)

data class UpdatePortfolioRequest(val name: String?, val baseCurrency: String?)