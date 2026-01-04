package com.notivest.portfolioservice.models

import com.notivest.portfolioservice.models.portfolio.PortfolioEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UuidGenerator
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

enum class HoldingMovementType {
    BUY,
    SELL,
}

@Entity
@Table(
    name = "holding_movements",
    indexes = [
        Index(name = "ix_holding_movements_portfolio", columnList = "portfolio_id"),
        Index(name = "ix_holding_movements_symbol", columnList = "symbol"),
    ],
)
class HoldingMovementEntity(
    @Id
    @GeneratedValue
    @UuidGenerator
    var id: UUID? = null,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "portfolio_id", nullable = false)
    var portfolio: PortfolioEntity,
    @Column(name = "holding_id")
    var holdingId: UUID? = null,
    @Column(name = "symbol", nullable = false, length = 15)
    var symbol: String,
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 4)
    var type: HoldingMovementType,
    @Column(name = "quantity", nullable = false, precision = 24, scale = 8)
    var quantity: BigDecimal,
    @Column(name = "price", nullable = false, precision = 24, scale = 8)
    var price: BigDecimal,
    @Column(name = "note", length = 200)
    var note: String? = null,
    @CreationTimestamp
    @Column(name = "executed_at", nullable = false, columnDefinition = "timestamptz")
    var executedAt: Instant? = null,
)
