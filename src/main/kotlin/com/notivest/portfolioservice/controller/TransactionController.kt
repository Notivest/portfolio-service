package com.notivest.portfolioservice.controller

import com.notivest.portfolioservice.controller.dto.PostTransactionRequest
import com.notivest.portfolioservice.controller.dto.TransactionResponse
import com.notivest.portfolioservice.service.interfaces.TransactionService
import jakarta.validation.Valid
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
@RequestMapping("/{pid}/transactions")
class TransactionController(
    private val service: TransactionService,
) {
    @PostMapping
    fun post(
        @PathVariable pid: UUID,
        @RequestHeader(name = "Idempotency-Key", required = false) idem: String?,
        principal: Principal,
        @Valid @RequestBody body: PostTransactionRequest,
    ): ResponseEntity<TransactionResponse> {
        val tx = service.post(principal.name, pid, body.toCmd(), idem)
        return ResponseEntity.status(HttpStatus.CREATED).body(TransactionResponse.fromEntity(tx))
    }

    @GetMapping("/{transactionId}")
    fun get(
        @PathVariable pid: UUID,
        @PathVariable transactionId: UUID,
        principal: Principal,
    ): ResponseEntity<TransactionResponse> {
        val transaction = service.get(principal.name, pid, transactionId)
        return ResponseEntity.ok(TransactionResponse.fromEntity(transaction))
    }

    @GetMapping
    fun search(
        @PathVariable pid: UUID,
        principal: Principal,
        @RequestParam(required = false) from: LocalDate?,
        @RequestParam(required = false) to: LocalDate?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): ResponseEntity<List<TransactionResponse>> {
        val transactionsPage = service.search(principal.name, pid, from, to, page, size)
        val transactions = transactionsPage.content.map { TransactionResponse.fromEntity(it) }
        return ResponseEntity.ok(transactions)
    }
}
