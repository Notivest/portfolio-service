package com.notivest.portfolio_service.controller.asdf

import com.notivest.portfolio_service.controller.dto.AccountResponse
import com.notivest.portfolio_service.controller.dto.CreateAccountRequest
import com.notivest.portfolio_service.controller.dto.UpdateAccountRequest
import com.notivest.portfolio_service.service.interfaces.AccountService
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
@RequestMapping("/v1/portfolios/{pid}/accounts")
class AccountController(
    private val service: AccountService
) {
    @PostMapping
    fun create(@PathVariable pid: UUID, @RequestBody body: CreateAccountRequest, principal: Principal): ResponseEntity<AccountResponse> {
        val acc = service.create(principal.name, pid, body.name, body.currency)
        return ResponseEntity.status(HttpStatus.CREATED).body(AccountResponse.fromEntity(acc))
    }

    @GetMapping("/{accountId}")
    fun get(@PathVariable pid: UUID, @PathVariable accountId: UUID, principal: Principal) =
        AccountResponse.fromEntity(service.get(principal.name, pid, accountId))

    @GetMapping
    fun list(@PathVariable pid: UUID, principal: Principal) =
        service.list(principal.name, pid).map { AccountResponse.fromEntity(it) }

    @PutMapping("/{accountId}")
    fun update(@PathVariable pid: UUID, @PathVariable accountId: UUID, @RequestBody body: UpdateAccountRequest, principal: Principal) =
        AccountResponse.fromEntity(service.update(principal.name, pid, accountId, body.name, body.currency))

    @DeleteMapping("/{accountId}")
    fun delete(@PathVariable pid: UUID, @PathVariable accountId: UUID, principal: Principal) {
        service.delete(principal.name, pid, accountId)
    }
}