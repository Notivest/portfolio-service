package com.notivest.portfolioservice.controller.dto

import com.notivest.portfolioservice.models.AccountEntity
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.util.UUID

data class CreateAccountRequest(
    @field:NotBlank(message = "Account name is required")
    @field:Size(min = 1, max = 50, message = "Account name must be between 1 and 50 characters")
    val name: String,
    @field:NotBlank(message = "Currency is required")
    @field:Size(min = 3, max = 3, message = "Currency must be exactly 3 characters")
    val currency: String,
)

data class UpdateAccountRequest(
    @field:Size(min = 1, max = 50, message = "Account name must be between 1 and 50 characters")
    val name: String?,
    @field:Size(min = 3, max = 3, message = "Currency must be exactly 3 characters")
    val currency: String?,
)

data class AccountResponse(val id: UUID, val name: String, val currency: String) {
    companion object {
        fun fromEntity(e: AccountEntity) = AccountResponse(e.id, e.name, e.currency)
    }
}
