package com.notivest.portfolioservice.controller

import com.notivest.portfolioservice.dto.portfolio.response.PortfolioWithHoldingsResponse
import com.notivest.portfolioservice.dto.holding.response.HoldingSummaryResponse
import com.notivest.portfolioservice.service.interfaces.HoldingService
import com.notivest.portfolioservice.service.interfaces.InternalPortfolioService
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@Profile("auth")
@RestController
@RequestMapping("/internal/v1/portfolios")
class InternalPortfolioController(
    private val internalPortfolioService: InternalPortfolioService,
    private val holdingService: HoldingService,
) {
    @GetMapping
    fun listPortfolios(
        @RequestParam userId: UUID,
        @RequestParam(required = false, defaultValue = "false") includeHoldings: Boolean,
    ): ResponseEntity<List<PortfolioWithHoldingsResponse>> {
        val response = internalPortfolioService.listPortfoliosForUser(userId, includeHoldings)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{portfolioId}/holdings/summary")
    fun getHoldingsSummary(
        @PathVariable portfolioId: UUID,
        @RequestParam userId: UUID,
        @RequestParam(defaultValue = "10") limit: Int,
        @RequestParam(defaultValue = "marketValue,desc") sort: String,
    ): ResponseEntity<List<HoldingSummaryResponse>> {
        if (limit < 1 || limit > 50) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "limit must be between 1 and 50",
            )
        }

        val response = holdingService.getSummary(userId, portfolioId, limit, sort)
        return ResponseEntity.ok(response)
    }
}
