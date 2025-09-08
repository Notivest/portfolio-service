package com.notivest.portfolioservice.service.interfaces

import com.notivest.portfolioservice.models.AccountEntity
import java.util.UUID

interface AccountService {
    fun create(
        userId: String,
        portfolioId: UUID,
        name: String,
        currency: String,
    ): AccountEntity

    fun get(
        userId: String,
        portfolioId: UUID,
        accountId: UUID,
    ): AccountEntity

    fun list(
        userId: String,
        portfolioId: UUID,
    ): List<AccountEntity>

    fun update(
        userId: String,
        portfolioId: UUID,
        accountId: UUID,
        name: String?,
        currency: String?,
    ): AccountEntity

    fun delete(
        userId: String,
        portfolioId: UUID,
        accountId: UUID,
    )
}
