package com.notivest.portfolioservice.service.interfaces

import com.notivest.portfolioservice.dto.portfolio.response.PortfolioWithHoldingsResponse
import java.util.UUID

interface InternalPortfolioService {
    fun listPortfoliosForUser(
        userId: UUID,
        includeHoldings: Boolean,
    ): List<PortfolioWithHoldingsResponse>
}
