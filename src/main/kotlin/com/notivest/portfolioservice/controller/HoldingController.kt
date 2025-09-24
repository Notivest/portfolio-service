package com.notivest.portfolioservice.controller

import com.notivest.portfolioservice.dto.holding.request.HoldingCreateRequest
import com.notivest.portfolioservice.dto.holding.request.HoldingUpdateRequest
import com.notivest.portfolioservice.dto.holding.response.HoldingResponse
import com.notivest.portfolioservice.security.JwtUserIdResolver
import com.notivest.portfolioservice.service.interfaces.HoldingService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import java.util.UUID

@RestController
@RequestMapping("/portfolios/{portfolioId}/holdings")
class HoldingController(
    private val service: HoldingService,
    private val userIdResolver: JwtUserIdResolver,
) {

    @GetMapping
    fun list(
        @PathVariable portfolioId: UUID,
        @RequestParam(name = "q", required = false) q: String?,
        @AuthenticationPrincipal jwt: Jwt,
        @PageableDefault(size = 20) pageable: Pageable
    ): Page<HoldingResponse> {
        val userId = userIdResolver.requireUserId(jwt)
        return service.list(userId, portfolioId, q, pageable)
    }

    @PostMapping
    fun create(
        @PathVariable portfolioId: UUID,
        @Valid @RequestBody body: HoldingCreateRequest,
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<HoldingResponse> {
        val userId = userIdResolver.requireUserId(jwt)
        val created = service.create(userId, portfolioId, body)
        return ResponseEntity.created(
            URI.create("/portfolios/$portfolioId/holdings/${created.id}")
        ).body(created)
    }

    @PatchMapping("/{holdingId}")
    fun patch(
        @PathVariable portfolioId: UUID,
        @PathVariable holdingId: UUID,
        @Valid @RequestBody body: HoldingUpdateRequest,
        @AuthenticationPrincipal jwt: Jwt
    ): HoldingResponse {
        val userId = userIdResolver.requireUserId(jwt)
        return service.update(userId, portfolioId, holdingId, body)
    }

    @DeleteMapping("/{holdingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @PathVariable portfolioId: UUID,
        @PathVariable holdingId: UUID,
        @AuthenticationPrincipal jwt: Jwt
    ) {
        val userId = userIdResolver.requireUserId(jwt)
        service.delete(userId, portfolioId, holdingId)
    }

}
