package com.notivest.portfolio_service.controller.dto

import com.notivest.portfolio_service.models.TransactionEntity
import com.notivest.portfolio_service.models.enums.TransactionType
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

data class PostTransactionRequest(
    val accountId: UUID,
    val type: TransactionType,
    val symbolId: String? = null,
    val qty: BigDecimal? = null,
    val price: BigDecimal? = null,
    val currency: String,
    val tradeDate: LocalDate,
    val settleDate: LocalDate? = null,
    val fees: BigDecimal = BigDecimal.ZERO,
    val taxes: BigDecimal = BigDecimal.ZERO,
    val fxRate: BigDecimal? = null,
    val note: String? = null,
    val source: String? = null
) {
    fun toCmd() = TransactionDTO(
        accountId, type, symbolId, qty, price,
        currency.uppercase(), tradeDate, settleDate,
        fees, taxes, fxRate, note, source
    )
}

data class TransactionResponse(
    val id: UUID,
    val portfolioId: UUID,
    val accountId: UUID,
    val symbolId: String?,
    val type: TransactionType,
    val qty: BigDecimal?,
    val price: BigDecimal?,
    val currency: String,
    val tradeDate: LocalDate,
    val settleDate: LocalDate?,
    val fees: BigDecimal,
    val taxes: BigDecimal,
    val fxRate: BigDecimal?,
    val note: String?,
    val source: String?,
    val idempotencyKey: String?
) {
    companion object {
        fun fromEntity(e: TransactionEntity) = TransactionResponse(
            id = e.id,
            portfolioId = e.portfolioId,
            accountId = e.accountId,
            symbolId = e.symbolId,
            type = e.type,
            qty = e.qty,
            price = e.price,
            currency = e.currency,
            tradeDate = e.tradeDate,
            settleDate = e.settleDate,
            fees = e.fees,
            taxes = e.taxes,
            fxRate = e.fxRate,
            note = e.note,
            source = e.source,
            idempotencyKey = e.idempotencyKey
        )
    }
}