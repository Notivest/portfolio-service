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
@Table(name = "portfolios",
    indexes = [Index(name = "ix_portfolios_user", columnList = "user_id")]
)
data class PortfolioEntity(
    @Id
    var id: UUID = UUID.randomUUID(),

    @Column(name = "user_id" , nullable = false , length = 64)
    val userId : String,

    @Column(nullable = false, length = 100)
    var name : String,

    @Column(name = "base_currency", nullable = false, length = 3)
    var baseCurrency : String,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: OffsetDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime? = null
)