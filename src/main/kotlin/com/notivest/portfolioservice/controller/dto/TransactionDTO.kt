package com.notivest.portfolioservice.controller.dto

import com.notivest.portfolioservice.models.enums.TransactionType
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

data class TransactionDTO(
    val accountId: UUID,
    val type: TransactionType,
    val symbolId: String?,
    val qty: BigDecimal?,
    val price: BigDecimal?,
    val currency: String,
    val tradeDate: LocalDate,
    val settleDate: LocalDate? = null,
    val fees: BigDecimal = BigDecimal.ZERO,
    val taxes: BigDecimal = BigDecimal.ZERO,
    val fxRate: BigDecimal? = null,
    val note: String? = null,
    val source: String? = null,
)
