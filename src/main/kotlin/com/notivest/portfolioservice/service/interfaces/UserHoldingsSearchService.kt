package com.notivest.portfolioservice.service.interfaces

import com.notivest.portfolioservice.dto.holding.response.HoldingSearchResponse
import java.util.UUID

interface UserHoldingsSearchService {
    fun search(
        userId: UUID,
        symbols: List<String>,
    ): List<HoldingSearchResponse>
}
