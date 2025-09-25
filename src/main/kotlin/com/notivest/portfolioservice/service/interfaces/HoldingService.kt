package com.notivest.portfolioservice.service.interfaces

import com.notivest.portfolioservice.dto.holding.request.HoldingCreateRequest
import com.notivest.portfolioservice.dto.holding.request.HoldingUpdateRequest
import com.notivest.portfolioservice.dto.holding.response.HoldingResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface HoldingService {
    fun list(
        userId: UUID,
        portfolioId: UUID,
        symbolFilter: String?,
        pageable: Pageable,
    ): Page<HoldingResponse>

    fun create(
        userId: UUID,
        portfolioId: UUID,
        req: HoldingCreateRequest,
    ): HoldingResponse

    fun update(
        userId: UUID,
        portfolioId: UUID,
        holdingId: UUID,
        req: HoldingUpdateRequest,
    ): HoldingResponse

    fun delete(
        userId: UUID,
        portfolioId: UUID,
        holdingId: UUID,
    )
}
