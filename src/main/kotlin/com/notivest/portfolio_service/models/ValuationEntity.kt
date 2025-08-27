package com.notivest.portfolio_service.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Lob
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(
    name = "valuations",
    indexes = [Index(name = "ix_val_portfolio_asof", columnList = "portfolio_id, as_of")]
)
data class ValuationEntity(
    @Id
    var id: UUID = UUID.randomUUID(),

    @Column(name = "portfolio_id", nullable = false)
    val portfolioId: UUID,

    // fecha y hora de valuacion
    @Column(name = "as_of", nullable = false)
    val asOf: OffsetDateTime,

    @Lob
    @Column(name = "totals", nullable = false, columnDefinition = "text")
    val totalsJson: String,

    @Lob
    @Column(name = "positions", nullable = false, columnDefinition = "text")
    val positionsJson: String,

    @Lob
    @Column(name = "fx_used", columnDefinition = "text")
    val fxUsedJson: String? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: OffsetDateTime? = null
)