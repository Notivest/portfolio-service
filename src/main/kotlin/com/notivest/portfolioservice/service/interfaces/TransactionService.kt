package com.notivest.portfolioservice.service.interfaces

import com.notivest.portfolioservice.controller.dto.TransactionDTO
import com.notivest.portfolioservice.models.TransactionEntity
import org.springframework.data.domain.Page
import java.time.LocalDate
import java.util.UUID

interface TransactionService {
    fun post(
        userId: String,
        portfolioId: UUID,
        req: TransactionDTO,
        idempotencyKey: String?,
    ): TransactionEntity

    fun get(
        userId: String,
        portfolioId: UUID,
        transactionId: UUID,
    ): TransactionEntity

    fun search(
        userId: String,
        portfolioId: UUID,
        from: LocalDate?,
        to: LocalDate?,
        page: Int,
        size: Int,
    ): Page<TransactionEntity>
}
