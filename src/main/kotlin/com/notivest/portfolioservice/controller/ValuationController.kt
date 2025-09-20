package com.notivest.portfolioservice.controller

import com.notivest.portfolioservice.controller.dto.RunValuationRequest
import com.notivest.portfolioservice.controller.dto.ValuationResponse
import com.notivest.portfolioservice.service.interfaces.ValuationService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.security.Principal
import java.time.OffsetDateTime
import java.util.UUID

@RestController
@RequestMapping("/{pid}/valuation")
class ValuationController(
    private val service: ValuationService,
) {
    @PostMapping("/run")
    fun run(
        @PathVariable pid: UUID,
        principal: Principal,
        @RequestBody(required = false) body: RunValuationRequest?,
    ): ResponseEntity<ValuationResponse> {
        val asOf = body?.asOf ?: OffsetDateTime.now()
        val snap = service.runValuation(principal.name, pid, asOf)
        return ResponseEntity.status(HttpStatus.CREATED).body(ValuationResponse.fromEntity(snap))
    }

    @GetMapping("/latest")
    fun latest(
        @PathVariable pid: UUID,
        principal: Principal,
    ): ResponseEntity<ValuationResponse> {
        val last =
            service.latest(principal.name, pid)
                ?: return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
        return ResponseEntity.ok(ValuationResponse.fromEntity(last))
    }

    @GetMapping("/history")
    fun history(
        @PathVariable pid: UUID,
        principal: Principal,
        @RequestParam from: OffsetDateTime,
        @RequestParam to: OffsetDateTime,
    ): ResponseEntity<List<ValuationResponse>> {
        val valuations =
            service.history(principal.name, pid, from, to)
                .map { ValuationResponse.fromEntity(it) }
        return ResponseEntity.ok(valuations)
    }
}
