package com.notivest.portfolio_service.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(
    name = "accounts",
    indexes = [Index(name = "ix_accounts_portfolio", columnList = "portfolio_id")]
)
data class AccountEntity (
    @Id
    var id: UUID = UUID.randomUUID(),

    @Column(name = "portfolio_id", nullable = false)
    var portfolioId: UUID,

    @Column(nullable = false, length = 100)
    var name: String,

    @Column(nullable = false, length = 3)
    var currency : String,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: OffsetDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime? = null
)