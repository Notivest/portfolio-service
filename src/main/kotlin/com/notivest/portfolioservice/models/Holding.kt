package com.notivest.portfolioservice.models

import com.notivest.portfolioservice.models.portfolio.PortfolioEntity
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.hibernate.annotations.UuidGenerator
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "holdings",
    uniqueConstraints = [
        UniqueConstraint(name = "uq_holdings_portfolio_symbol", columnNames = ["portfolio_id", "symbol"]),
    ],
    indexes = [
        Index(name = "ix_holdings_portfolio", columnList = "portfolio_id"),
        Index(name = "ix_holdings_symbol", columnList = "symbol"),
    ],
)
class HoldingEntity(
    @Id
    @GeneratedValue
    @UuidGenerator
    var id: UUID? = null,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "portfolio_id", nullable = false)
    var portfolio: PortfolioEntity,
    @Column(name = "symbol", nullable = false, length = 15)
    var symbol: String,
    @Column(name = "quantity", precision = 24, scale = 8)
    var quantity: BigDecimal? = null,
    @Column(name = "avg_cost", precision = 24, scale = 8)
    var avgCost: BigDecimal? = null,
    @Column(name = "note", length = 200)
    var note: String? = null,
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "timestamptz")
    var createdAt: Instant? = null,
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "timestamptz")
    var updatedAt: Instant? = null,
    @Version
    var version: Long? = null,
)
