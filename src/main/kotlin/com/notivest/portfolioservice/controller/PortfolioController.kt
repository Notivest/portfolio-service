package com.notivest.portfolioservice.controller

import com.notivest.portfolioservice.controller.dto.CreatePortfolioRequest
import com.notivest.portfolioservice.controller.dto.PortfolioResponse
import com.notivest.portfolioservice.controller.dto.UpdatePortfolioRequest
import com.notivest.portfolioservice.service.interfaces.PortfolioService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.security.Principal
import java.util.UUID

@RestController
@RequestMapping("/v1/portfolios")
class PortfolioController(
    private val service: PortfolioService,
) {
    @PostMapping
    fun create(
        @Valid @RequestBody body: CreatePortfolioRequest,
        principal: Principal,
    ): ResponseEntity<PortfolioResponse> {
        val p = service.create(principal.name, body.name, body.baseCurrency)
        return ResponseEntity.status(HttpStatus.CREATED).body(PortfolioResponse.fromEntity(p))
    }

    @GetMapping("/{id}")
    fun get(
        @PathVariable id: UUID,
        principal: Principal,
    ): ResponseEntity<PortfolioResponse> {
        val portfolio = service.get(principal.name, id)
        return ResponseEntity.ok(PortfolioResponse.fromEntity(portfolio))
    }

    @GetMapping
    fun list(
        principal: Principal,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): ResponseEntity<List<PortfolioResponse>> {
        val portfoliosPage = service.list(principal.name, page, size)
        val portfolios = portfoliosPage.content.map { PortfolioResponse.fromEntity(it) }
        return ResponseEntity.ok(portfolios)
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: UUID,
        @Valid @RequestBody body: UpdatePortfolioRequest,
        principal: Principal,
    ): ResponseEntity<PortfolioResponse> {
        val updatedPortfolio = service.update(principal.name, id, body.name, body.baseCurrency)
        return ResponseEntity.ok(PortfolioResponse.fromEntity(updatedPortfolio))
    }

    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: UUID,
        principal: Principal,
    ): ResponseEntity<Void> {
        service.delete(principal.name, id)
        return ResponseEntity.noContent().build()
    }
}
