package com.notivest.portfolioservice.repository

import com.notivest.portfolioservice.models.HoldingEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface HoldingRepository : JpaRepository<HoldingEntity, UUID>
