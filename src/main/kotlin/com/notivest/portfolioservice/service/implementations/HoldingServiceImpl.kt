package com.notivest.portfolioservice.service.implementations

import com.notivest.portfolioservice.dto.holding.applyTo
import com.notivest.portfolioservice.dto.holding.request.HoldingCreateRequest
import com.notivest.portfolioservice.dto.holding.request.HoldingUpdateRequest
import com.notivest.portfolioservice.dto.holding.response.HoldingResponse
import com.notivest.portfolioservice.dto.holding.toEntity
import com.notivest.portfolioservice.dto.holding.toResponse
import com.notivest.portfolioservice.repository.HoldingRepository
import com.notivest.portfolioservice.repository.PortfolioRepository
import com.notivest.portfolioservice.service.interfaces.HoldingService
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.util.Locale
import java.util.UUID

@Service
class HoldingServiceImpl(
    private val repo: HoldingRepository,
    private val portfolioRepo: PortfolioRepository,
) : HoldingService{

    @Transactional(readOnly = true)
    override fun list(
        userId: UUID,
        portfolioId: UUID,
        symbolFilter: String?,
        pageable: Pageable,
    ): Page<HoldingResponse> {
        requirePortfolioOwned(userId, portfolioId)

        val page = if (symbolFilter.isNullOrBlank()) {
            repo.findAllByPortfolioId(portfolioId, pageable)
        } else {
            repo.findAllByPortfolioIdAndSymbolContainingIgnoreCase(
                portfolioId, symbolFilter.trim(), pageable
            )
        }
        return page.map { it.toResponse() }
    }

    override fun create(
        userId: UUID,
        portfolioId: UUID,
        req: HoldingCreateRequest,
    ): HoldingResponse {
        val portfolio = requirePortfolioOwned(userId, portfolioId)

        // normalización de símbolo
        val normalized = req.copy(symbol = req.symbol.trim().uppercase(Locale.US))

        val entity = normalized.toEntity(portfolio).also {
            // por si acaso, aseguramos uppercase
            it.symbol = it.symbol.uppercase(Locale.US)
        }

        try {
            val saved = repo.saveAndFlush(entity)
            return saved.toResponse()
        } catch (ex: DataIntegrityViolationException) {
            // Mapeamos la violación de UNIQUE (portfolio_id, symbol) a 409
            throw ResponseStatusException(HttpStatus.CONFLICT, "Holding for symbol already exists in this portfolio" + ex.message)
        }    }

    override fun update(
        userId: UUID,
        portfolioId: UUID,
        holdingId: UUID,
        req: HoldingUpdateRequest,
    ): HoldingResponse {
        requirePortfolioOwned(userId, portfolioId)

        val entity = repo.findByIdAndPortfolioId(holdingId, portfolioId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Holding not found") }

        // apply only non-null fields
        req.applyTo(entity)

        val saved = repo.saveAndFlush(entity)
        return saved.toResponse()
    }

    override fun delete(userId: UUID, portfolioId: UUID, holdingId: UUID) {
        requirePortfolioOwned(userId, portfolioId)

        val entity = repo.findByIdAndPortfolioId(holdingId, portfolioId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Holding not found") }

        repo.delete(entity)
    }

    private fun requirePortfolioOwned(userId: UUID, portfolioId: UUID) =
        portfolioRepo.findByIdAndUserIdAndDeletedAtIsNull(portfolioId, userId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Portfolio not found") }
}