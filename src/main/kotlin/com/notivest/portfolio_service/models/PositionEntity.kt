package com.notivest.portfolio_service.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.UpdateTimestamp
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.UUID


@Entity
@Table(
    name = "positions",
    uniqueConstraints = [UniqueConstraint(columnNames = ["portfolio_id", "account_id", "symbol_id"])],
    indexes = [Index(name = "ix_pos_portfolio", columnList = "portfolio_id")]
)
data class PositionEntity (
    @Id
    var id: UUID = UUID.randomUUID(),

    @Column(name = "portfolio_id" , nullable = false)
    val portfolioId : UUID,

    @Column(name = "account_id", nullable = false)
    val accountId : UUID,

    @Column(name = "symbol_id", nullable = false, length = 32)
    val symbolId : String,

    @Column(nullable = false)
    var qty : BigDecimal = BigDecimal.ZERO,

    @Column(nullable = false)
    var avgCost : BigDecimal = BigDecimal.ZERO,

    @Column(nullable = false , length = 3)
    var currency : String = "USD",

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime? = null
)