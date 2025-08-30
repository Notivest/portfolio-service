package com.notivest.portfolio_service.controller.asdf

import com.notivest.portfolio_service.controller.dto.PostTransactionRequest
import com.notivest.portfolio_service.controller.dto.TransactionResponse
import com.notivest.portfolio_service.service.interfaces.TransactionService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.security.Principal
import java.time.LocalDate
import java.util.UUID

@RestController
@RequestMapping("/v1/portfolios/{pid}/transactions")
class TransactionController(
    private val service: TransactionService
) {
    @PostMapping
    fun post(@PathVariable pid: UUID,
             @RequestHeader(name = "Idempotency-Key", required = false) idem: String?,
             principal: Principal,
             @RequestBody body: PostTransactionRequest
    ): ResponseEntity<TransactionResponse> {
        val tx = service.post(principal.name, pid, body.toCmd(), idem)
        return ResponseEntity.status(HttpStatus.CREATED).body(TransactionResponse.fromEntity(tx))
    }

    @GetMapping("/{transactionId}")
    fun get(@PathVariable pid: UUID, @PathVariable transactionId: UUID, principal: Principal) =
        TransactionResponse.fromEntity(service.get(principal.name, pid, transactionId))

    @GetMapping
    fun search(@PathVariable pid: UUID,
               principal: Principal,
               @RequestParam(required = false) from: LocalDate?,
               @RequestParam(required = false) to: LocalDate?,
               @RequestParam(defaultValue = "0") page: Int,
               @RequestParam(defaultValue = "20") size: Int) =
        service.search(principal.name, pid, from, to, page, size).map { TransactionResponse.fromEntity(it) }
}