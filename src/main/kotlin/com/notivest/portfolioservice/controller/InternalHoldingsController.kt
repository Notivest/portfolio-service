package com.notivest.portfolioservice.controller

import com.notivest.portfolioservice.dto.holding.request.SearchHoldingsRequest
import com.notivest.portfolioservice.dto.holding.response.HoldingSearchResponse
import com.notivest.portfolioservice.service.interfaces.UserHoldingsSearchService
import jakarta.validation.ConstraintViolationException
import jakarta.validation.Valid
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Profile("auth")
@RestController
@RequestMapping("/internal/v1/holdings")
class InternalHoldingsController(
    private val userHoldingsSearchService: UserHoldingsSearchService,
) {
    @PostMapping("/search")
    fun search(
        @Valid @RequestBody body: SearchHoldingsRequest,
    ): ResponseEntity<*> {
        try{
            val requestUserId =
                body.userId ?: throw ConstraintViolationException("userId must be provided", emptySet())

            val holdings = userHoldingsSearchService.search(requestUserId, body.symbols)
            return ResponseEntity.ok(holdings)
        } catch (e : Exception) {
            return ResponseEntity.badRequest().body(e.message)
        }
    }
}
