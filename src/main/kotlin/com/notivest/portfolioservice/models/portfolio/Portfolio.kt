package com.notivest.portfolioservice.models.portfolio

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.hibernate.annotations.UuidGenerator
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "portfolios", indexes = [Index(name = "ix_portfolios_user", columnList = "user_id")])
class PortfolioEntity(
    @Id
    @GeneratedValue
    @UuidGenerator
    var id: UUID? = null,
    @Column(name = "user_id", nullable = false)
    var userId: UUID,
    @Column(name = "name", nullable = false, length = 80)
    var name: String,
    @Column(name = "base_currency", nullable = false, length = 3)
    var baseCurrency: String,
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    var status: PortfolioStatus = PortfolioStatus.ACTIVE,
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "timestamptz")
    var createdAt: Instant? = null,
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "timestamptz")
    var updatedAt: Instant? = null,
    @Column(name = "deleted_at")
    var deletedAt: Instant? = null,
)
