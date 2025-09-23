package com.notivest.portfolioservice.controller

import com.notivest.portfolioservice.dto.portfolio.request.PortfolioCreateRequest
import com.notivest.portfolioservice.dto.portfolio.request.PortfolioUpdateRequest
import com.notivest.portfolioservice.dto.portfolio.response.PortfolioResponse
import com.notivest.portfolioservice.security.JwtUserIdResolver
import com.notivest.portfolioservice.service.interfaces.PortfolioService
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
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import java.util.UUID

@RestController
@RequestMapping("/portfolios")
class PortfolioController(
    private val service: PortfolioService,
    private val userIdResolver: JwtUserIdResolver,
) {
    @GetMapping
    fun list(
        @AuthenticationPrincipal jwt: Jwt,
        @PageableDefault(size = 20) pageable: Pageable,
    ): Page<PortfolioResponse> {
        val userId = userIdResolver.requireUserId(jwt)
        return service.list(userId, pageable)
    }

    @GetMapping("/{id}")
    fun getOne(
        @PathVariable id: UUID,
        @AuthenticationPrincipal jwt: Jwt,
    ): PortfolioResponse {
        val userId = userIdResolver.requireUserId(jwt)
        return service.get(userId, id)
    }

    @PostMapping
    fun create(
        @Valid @RequestBody body: PortfolioCreateRequest,
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<PortfolioResponse> {
        val userId = userIdResolver.requireUserId(jwt)
        val created = service.create(userId, body)
        return ResponseEntity.created(URI.create("/portfolios/${created.id}")).body(created)
    }

    @PatchMapping("/{id}")
    fun patch(
        @PathVariable id: UUID,
        @Valid @RequestBody body: PortfolioUpdateRequest,
        @AuthenticationPrincipal jwt: Jwt,
    ): PortfolioResponse {
        val userId = userIdResolver.requireUserId(jwt)
        return service.update(userId, id, body)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @PathVariable id: UUID,
        @AuthenticationPrincipal jwt: Jwt,
    ) {
        val userId = userIdResolver.requireUserId(jwt)
        service.delete(userId, id)
    }
}
