package com.notivest.portfolioservice.controller

import com.notivest.portfolioservice.dto.portfolio.response.PortfolioWithHoldingsResponse
import com.notivest.portfolioservice.service.interfaces.InternalPortfolioService
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@Profile("auth")
@RestController
@RequestMapping("/internal/v1/portfolios")
class InternalPortfolioController(
    private val internalPortfolioService: InternalPortfolioService,
) {
    @GetMapping
    fun listPortfolios(
        @RequestParam userId: UUID,
        @RequestParam(required = false, defaultValue = "false") includeHoldings: Boolean,
    ): ResponseEntity<List<PortfolioWithHoldingsResponse>> {
        val response = internalPortfolioService.listPortfoliosForUser(userId, includeHoldings)
        return ResponseEntity.ok(response)
    }
}
