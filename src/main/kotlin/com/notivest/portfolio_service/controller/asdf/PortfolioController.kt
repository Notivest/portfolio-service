package com.notivest.portfolio_service.controller.asdf

import com.notivest.portfolio_service.controller.dto.CreatePortfolioRequest
import com.notivest.portfolio_service.controller.dto.PortfolioResponse
import com.notivest.portfolio_service.controller.dto.UpdatePortfolioRequest
import com.notivest.portfolio_service.service.interfaces.PortfolioService
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
    private val service: PortfolioService
) {
    @PostMapping
    fun create(@RequestBody body: CreatePortfolioRequest, principal: Principal): ResponseEntity<PortfolioResponse> {
        val p = service.create(principal.name, body.name, body.baseCurrency)
        return ResponseEntity.status(HttpStatus.CREATED).body(PortfolioResponse.fromEntity(p))
    }

    @GetMapping("/{id}")
    fun get(@PathVariable id: UUID, principal: Principal) =
        PortfolioResponse.fromEntity(service.get(principal.name, id))

    @GetMapping
    fun list(principal: Principal,
             @RequestParam(defaultValue = "0") page: Int,
             @RequestParam(defaultValue = "20") size: Int) =
        service.list(principal.name, page, size).map { PortfolioResponse.fromEntity(it) }

    @PutMapping("/{id}")
    fun update(@PathVariable id: UUID, @RequestBody body: UpdatePortfolioRequest, principal: Principal) =
        PortfolioResponse.fromEntity(service.update(principal.name, id, body.name, body.baseCurrency))

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: UUID, principal: Principal) {
        service.delete(principal.name, id)
    }
}