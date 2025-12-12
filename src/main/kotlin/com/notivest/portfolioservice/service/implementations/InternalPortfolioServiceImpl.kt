package com.notivest.portfolioservice.service.implementations

import com.notivest.portfolioservice.dto.holding.toPortfolioHoldingResponse
import com.notivest.portfolioservice.dto.portfolio.response.PortfolioHoldingResponse
import com.notivest.portfolioservice.dto.portfolio.response.PortfolioWithHoldingsResponse
import com.notivest.portfolioservice.dto.portfolio.toResponseWithHoldings
import com.notivest.portfolioservice.models.HoldingEntity
import com.notivest.portfolioservice.repository.HoldingRepository
import com.notivest.portfolioservice.repository.PortfolioRepository
import com.notivest.portfolioservice.service.interfaces.InternalPortfolioService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class InternalPortfolioServiceImpl(
    private val portfolioRepository: PortfolioRepository,
    private val holdingRepository: HoldingRepository,
) : InternalPortfolioService {
    @Transactional(readOnly = true)
    override fun listPortfoliosForUser(
        userId: UUID,
        includeHoldings: Boolean,
    ): List<PortfolioWithHoldingsResponse> {
        val portfolios = portfolioRepository.findAllByUserIdAndDeletedAtIsNull(userId)
        if (portfolios.isEmpty()) {
            return emptyList()
        }

        val holdingsByPortfolioId: Map<UUID, List<PortfolioHoldingResponse>> =
            if (includeHoldings) {
                holdingRepository.findByUserId(userId)
                    .groupBy(
                        keySelector = { portfolioIdOf(it) },
                        valueTransform = { it.toPortfolioHoldingResponse() },
                    )
            } else {
                emptyMap()
            }

        return portfolios.map { portfolio ->
            val portfolioId = requireNotNull(portfolio.id) { "PortfolioEntity.id must not be null" }
            portfolio.toResponseWithHoldings(holdingsByPortfolioId[portfolioId])
        }
    }

    private fun portfolioIdOf(holding: HoldingEntity): UUID =
        holding.portfolio.id
            ?: throw IllegalStateException("Holding ${holding.id} references a portfolio without id")
}
