package com.notivest.portfolioservice.controller.dto

import com.notivest.portfolioservice.controller.dto.validation.ValidTransaction
import com.notivest.portfolioservice.models.TransactionEntity
import com.notivest.portfolioservice.models.enums.TransactionType
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

@ValidTransaction
data class PostTransactionRequest(
    @field:NotNull(message = "Account ID is required")
    val accountId: UUID,
    @field:NotNull(message = "Transaction type is required")
    val type: TransactionType,
    val symbolId: String? = null,
    @field:DecimalMin(value = "0.0", inclusive = false, message = "Quantity must be greater than 0")
    val qty: BigDecimal? = null,
    @field:DecimalMin(value = "0.0", inclusive = true, message = "Price must be greater than or equal to 0")
    val price: BigDecimal? = null,
    @field:NotBlank(message = "Currency is required")
    @field:Size(min = 3, max = 3, message = "Currency must be exactly 3 characters")
    val currency: String,
    @field:NotNull(message = "Trade date is required")
    val tradeDate: LocalDate,
    val settleDate: LocalDate? = null,
    @field:DecimalMin(value = "0.0", inclusive = true, message = "Fees must be greater than or equal to 0")
    val fees: BigDecimal = BigDecimal.ZERO,
    @field:DecimalMin(value = "0.0", inclusive = true, message = "Taxes must be greater than or equal to 0")
    val taxes: BigDecimal = BigDecimal.ZERO,
    val fxRate: BigDecimal? = null,
    val note: String? = null,
    val source: String? = null,
) {
    fun toCmd() =
        TransactionDTO(
            accountId, type, symbolId, qty, price,
            currency.uppercase(), tradeDate, settleDate,
            fees, taxes, fxRate, note, source,
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
    val idempotencyKey: String?,
) {
    companion object {
        fun fromEntity(e: TransactionEntity) =
            TransactionResponse(
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
                idempotencyKey = e.idempotencyKey,
            )
    }
}
