package com.notivest.portfolio_service.service.impl

import com.fasterxml.jackson.databind.ObjectMapper
import com.notivest.portfolio_service.models.ValuationEntity
import com.notivest.portfolio_service.repository.PositionRepository
import com.notivest.portfolio_service.repository.ValuationRepository
import com.notivest.portfolio_service.service.interfaces.PortfolioService
import com.notivest.portfolio_service.service.interfaces.ValuationService
import com.notivest.portfolio_service.valuation.ValuationCalculator
import com.notivest.portfolio_service.valuation.models.PositionInput
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

@Service
class ValuationServiceImpl(
    private val portfolioService : PortfolioService,
    private val positionRepository: PositionRepository,
    private val valuationRepository : ValuationRepository,
    private val calculator: ValuationCalculator,
    private val mapper: ObjectMapper
) : ValuationService{

    @Transactional
    override fun runValuation(userId: String, portfolioId: UUID, asOf: OffsetDateTime): ValuationEntity {
        val portfolio = portfolioService.get(userId, portfolioId)
        val base = portfolio.baseCurrency

        // 1) Preparar inputs
        val inputs = positionRepository.findAllByPortfolioId(portfolioId).map {
            PositionInput(
                symbolId = it.symbolId,
                qty = it.qty,
                avgCost = it.avgCost,
                currency = it.currency
            )
        }

        // 2) Calcular
        val result = calculator.compute(inputs, base)

        // 3) Persistir snapshot
        val snapshot = ValuationEntity(
            portfolioId = portfolioId,
            asOf = asOf,
            totalsJson = mapper.writeValueAsString(mapOf(
                "NAV" to result.nav,
                "PnLDaily" to 0,  // completar cuando tengas series
                "PnLYTD" to 0,
                "cash" to emptyMap<String, Any>() // integrar cuando modeles cash
            )),
            positionsJson = mapper.writeValueAsString(result.positions),
            fxUsedJson = mapper.writeValueAsString(result.fxUsed)
        )
        return valuationRepository.save(snapshot)
    }

    override fun latest(userId: String, portfolioId: UUID) =
        valuationRepository.findTop1ByPortfolioIdOrderByAsOfDesc(portfolioId)

    override fun history(userId: String, portfolioId: UUID, from: OffsetDateTime, to: OffsetDateTime) =
        valuationRepository.findAllByPortfolioIdAndAsOfBetween(portfolioId, from, to)
}
