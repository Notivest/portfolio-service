package com.notivest.portfolioservice.models

import com.notivest.portfolioservice.models.enums.TransactionType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.CreationTimestamp
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(
    name = "transactions",
    uniqueConstraints = [UniqueConstraint(columnNames = ["idempotency_key"])],
    indexes = [
        Index(name = "ix_tx_portfolio_date", columnList = "portfolio_id, trade_date"),
        Index(name = "ix_tx_portfolio", columnList = "portfolio_id"),
    ],
)
data class TransactionEntity(
    @Id
    var id: UUID = UUID.randomUUID(),
    @Column(name = "portfolio_id", nullable = false)
    val portfolioId: UUID,
    @Column(name = "account_id", nullable = false)
    val accountId: UUID,
    @Column(name = "symbol_id", length = 32)
    val symbolId: String? = null,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val type: TransactionType,
    @Column
    val qty: BigDecimal? = null,
    @Column
    val price: BigDecimal? = null,
    @Column(nullable = false)
    val fees: BigDecimal = BigDecimal.ZERO,
    @Column(nullable = false)
    val taxes: BigDecimal = BigDecimal.ZERO,
    @Column(nullable = false, length = 3)
    val currency: String,
    @Column(name = "trade_date", nullable = false)
    val tradeDate: LocalDate,
    @Column(name = "settle_date")
    val settleDate: LocalDate? = null,
    @Column(name = "fx_rate")
    val fxRate: BigDecimal? = null,
    @Column
    val note: String? = null,
    @Column
    val source: String? = null,
    @Column(name = "idempotency_key", length = 64)
    val idempotencyKey: String? = null,
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: OffsetDateTime? = null,
)
