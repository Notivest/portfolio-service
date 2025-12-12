package com.notivest.portfolioservice.service.implementations

import com.notivest.portfolioservice.dto.portfolio.applyTo
import com.notivest.portfolioservice.dto.portfolio.request.PortfolioCreateRequest
import com.notivest.portfolioservice.dto.portfolio.request.PortfolioUpdateRequest
import com.notivest.portfolioservice.dto.portfolio.response.PortfolioResponse
import com.notivest.portfolioservice.dto.portfolio.toEntity
import com.notivest.portfolioservice.dto.portfolio.toResponse
import com.notivest.portfolioservice.exception.NotFoundException
import com.notivest.portfolioservice.repository.PortfolioRepository
import com.notivest.portfolioservice.service.interfaces.PortfolioService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class PortfolioServiceImpl(
    private val portfolioRepository: PortfolioRepository,
) : PortfolioService {
    @Transactional(readOnly = true)
    override fun list(
        userId: UUID,
        pageable: Pageable,
    ): Page<PortfolioResponse> {
        return portfolioRepository.findAllByUserIdAndDeletedAtIsNull(userId, pageable)
            .map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    override fun get(
        userId: UUID,
        id: UUID,
    ): PortfolioResponse {
        return portfolioRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId)
            .orElseThrow { NotFoundException("Portfolio not found") }
            .toResponse()
    }

    @Transactional
    override fun create(
        userId: UUID,
        req: PortfolioCreateRequest,
    ): PortfolioResponse {
        val entity = req.toEntity(userId)
        val saved = portfolioRepository.saveAndFlush(entity)
        return saved.toResponse()
    }

    @Transactional
    override fun update(
        userId: UUID,
        id: UUID,
        req: PortfolioUpdateRequest,
    ): PortfolioResponse {
        val entity =
            portfolioRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId)
                .orElseThrow { NotFoundException("Portfolio not found") }
        req.applyTo(entity)
        val saved = portfolioRepository.saveAndFlush(entity)
        return saved.toResponse()
    }

    @Transactional
    override fun delete(
        userId: UUID,
        id: UUID,
    ) {
        val entity =
            portfolioRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId)
                .orElseThrow { NotFoundException("Portfolio not found") }
        entity.deletedAt = Instant.now()
        portfolioRepository.save(entity)
    }
}
