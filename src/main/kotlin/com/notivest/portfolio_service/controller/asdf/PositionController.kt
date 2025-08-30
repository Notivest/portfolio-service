package com.notivest.portfolio_service.controller.asdf

import com.notivest.portfolio_service.controller.dto.PositionResponse
import com.notivest.portfolio_service.controller.dto.RecomputePositionRequest
import com.notivest.portfolio_service.service.interfaces.PositionService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal
import java.util.UUID

@RestController
@RequestMapping("/v1/portfolios/{pid}/positions")
class PositionController(
    private val service: PositionService
) {

    @GetMapping
    fun list(
        @PathVariable pid : UUID,
        principal: Principal
    ) : List<PositionResponse> {
        return service.list(principal.name, pid).map { PositionResponse.fromEntity(it) }
    }

    @GetMapping("/{symbolId}")
    fun getOne(
        @PathVariable pid : UUID,
        @PathVariable symbolId : String,
        principal: Principal
    ) : PositionResponse? {
        return service.getOne(principal.name, pid, symbolId)?.let { PositionResponse.fromEntity(it) }
    }

    @PostMapping("/recompute")
    fun recompute(
        @PathVariable pid: UUID,
        principal: Principal,
        @RequestBody body: RecomputePositionRequest
    ): ResponseEntity<PositionResponse> {
        val pos = service.recomputeFor(
            userId = principal.name,
            portfolioId = pid,
            accountId = body.accountId,
            symbolId = body.symbolId
        )
        return ResponseEntity.status(HttpStatus.OK).body(PositionResponse.fromEntity(pos))
    }
}