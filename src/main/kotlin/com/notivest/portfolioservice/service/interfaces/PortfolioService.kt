package com.notivest.portfolioservice.service.interfaces

import com.notivest.portfolioservice.dto.portfolio.request.PortfolioCreateRequest
import com.notivest.portfolioservice.dto.portfolio.request.PortfolioUpdateRequest
import com.notivest.portfolioservice.dto.portfolio.response.PortfolioResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface PortfolioService {
    fun list(userId: UUID, pageable: Pageable): Page<PortfolioResponse>
    fun get(userId: UUID, id: UUID): PortfolioResponse
    fun create(userId: UUID, req: PortfolioCreateRequest): PortfolioResponse
    fun update(userId: UUID, id: UUID, req: PortfolioUpdateRequest): PortfolioResponse
    fun delete(userId: UUID, id: UUID)
}