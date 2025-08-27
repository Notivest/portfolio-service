package com.notivest.portfolio_service.controller.dto

import com.notivest.portfolio_service.models.AccountEntity
import java.util.UUID

data class CreateAccountRequest(val name: String, val currency: String)
data class UpdateAccountRequest(val name: String?, val currency: String?)
data class AccountResponse(val id: UUID, val name: String, val currency: String) {
    companion object { fun fromEntity(e: AccountEntity) = AccountResponse(e.id, e.name, e.currency) }
}