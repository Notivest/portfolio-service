package com.notivest.portfolioservice.controller

import com.notivest.portfolioservice.controller.dto.PositionResponse
import com.notivest.portfolioservice.controller.dto.RecomputePositionRequest
import com.notivest.portfolioservice.service.interfaces.PositionService
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
@RequestMapping("/{pid}/positions")
class PositionController(
    private val service: PositionService,
) {
    @GetMapping
    fun list(
        @PathVariable pid: UUID,
        principal: Principal,
    ): ResponseEntity<List<PositionResponse>> {
        val positions = service.list(principal.name, pid).map { PositionResponse.fromEntity(it) }
        return ResponseEntity.ok(positions)
    }

    @GetMapping("/{symbolId}")
    fun getOne(
        @PathVariable pid: UUID,
        @PathVariable symbolId: String,
        principal: Principal,
    ): ResponseEntity<PositionResponse> {
        val position = service.getOne(principal.name, pid, symbolId)
        return if (position != null) {
            ResponseEntity.ok(PositionResponse.fromEntity(position))
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping("/recompute")
    fun recompute(
        @PathVariable pid: UUID,
        principal: Principal,
        @RequestBody body: RecomputePositionRequest,
    ): ResponseEntity<PositionResponse> {
        val pos =
            service.recomputeFor(
                userId = principal.name,
                portfolioId = pid,
                accountId = body.accountId,
                symbolId = body.symbolId,
            )
        return ResponseEntity.status(HttpStatus.OK).body(PositionResponse.fromEntity(pos))
    }
}
