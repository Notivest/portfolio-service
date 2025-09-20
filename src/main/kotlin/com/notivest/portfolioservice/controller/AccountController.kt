package com.notivest.portfolioservice.controller

import com.notivest.portfolioservice.controller.dto.AccountResponse
import com.notivest.portfolioservice.controller.dto.CreateAccountRequest
import com.notivest.portfolioservice.controller.dto.UpdateAccountRequest
import com.notivest.portfolioservice.service.interfaces.AccountService
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
import org.springframework.web.bind.annotation.RestController
import java.security.Principal
import java.util.UUID

@RestController
@RequestMapping("/{pid}/accounts")
class AccountController(
    private val service: AccountService,
) {
    @PostMapping
    fun create(
        @PathVariable pid: UUID,
        @Valid @RequestBody body: CreateAccountRequest,
        principal: Principal,
    ): ResponseEntity<AccountResponse> {
        val acc = service.create(principal.name, pid, body.name, body.currency)
        return ResponseEntity.status(HttpStatus.CREATED).body(AccountResponse.fromEntity(acc))
    }

    @GetMapping("/{accountId}")
    fun get(
        @PathVariable pid: UUID,
        @PathVariable accountId: UUID,
        principal: Principal,
    ): ResponseEntity<AccountResponse> {
        val account = service.get(principal.name, pid, accountId)
        return ResponseEntity.ok(AccountResponse.fromEntity(account))
    }

    @GetMapping
    fun list(
        @PathVariable pid: UUID,
        principal: Principal,
    ): ResponseEntity<List<AccountResponse>> {
        val accounts = service.list(principal.name, pid).map { AccountResponse.fromEntity(it) }
        return ResponseEntity.ok(accounts)
    }

    @PutMapping("/{accountId}")
    fun update(
        @PathVariable pid: UUID,
        @PathVariable accountId: UUID,
        @Valid @RequestBody body: UpdateAccountRequest,
        principal: Principal,
    ): ResponseEntity<AccountResponse> {
        val updatedAccount = service.update(principal.name, pid, accountId, body.name, body.currency)
        return ResponseEntity.ok(AccountResponse.fromEntity(updatedAccount))
    }

    @DeleteMapping("/{accountId}")
    fun delete(
        @PathVariable pid: UUID,
        @PathVariable accountId: UUID,
        principal: Principal,
    ): ResponseEntity<Void> {
        service.delete(principal.name, pid, accountId)
        return ResponseEntity.noContent().build()
    }
}
