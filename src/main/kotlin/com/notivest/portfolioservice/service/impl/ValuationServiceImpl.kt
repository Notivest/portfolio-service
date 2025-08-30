package com.notivest.portfolioservice.service.impl

import com.fasterxml.jackson.databind.ObjectMapper
import com.notivest.portfolioservice.models.ValuationEntity
import com.notivest.portfolioservice.repository.PositionRepository
import com.notivest.portfolioservice.repository.ValuationRepository
import com.notivest.portfolioservice.service.interfaces.PortfolioService
import com.notivest.portfolioservice.service.interfaces.ValuationService
import com.notivest.portfolioservice.valuation.ValuationCalculator
import com.notivest.portfolioservice.valuation.models.PositionInput
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

@Service
class ValuationServiceImpl(
    private val portfolioService: PortfolioService,
    private val positionRepository: PositionRepository,
    private val valuationRepository: ValuationRepository,
    private val calculator: ValuationCalculator,
    private val mapper: ObjectMapper,
) : ValuationService {
    @Transactional
    override fun runValuation(
        userId: String,
        portfolioId: UUID,
        asOf: OffsetDateTime,
    ): ValuationEntity {
        val portfolio = portfolioService.get(userId, portfolioId)
        val base = portfolio.baseCurrency

        // 1) Preparar inputs
        val inputs =
            positionRepository.findAllByPortfolioId(portfolioId).map {
                PositionInput(
                    symbolId = it.symbolId,
                    qty = it.qty,
                    avgCost = it.avgCost,
                    currency = it.currency,
                )
            }

        // 2) Calcular
        val result = calculator.compute(inputs, base)

        // 3) Persistir snapshot
        val snapshot =
            ValuationEntity(
                portfolioId = portfolioId,
                asOf = asOf,
                totalsJson =
                    mapper.writeValueAsString(
                        mapOf(
                            "NAV" to result.nav,
                            // completar cuando tengas series
                            "PnLDaily" to 0,
                            "PnLYTD" to 0,
                            // integrar cuando modeles cash
                            "cash" to emptyMap<String, Any>(),
                        ),
                    ),
                positionsJson = mapper.writeValueAsString(result.positions),
                fxUsedJson = mapper.writeValueAsString(result.fxUsed),
            )
        return valuationRepository.save(snapshot)
    }

    override fun latest(
        userId: String,
        portfolioId: UUID,
    ): ValuationEntity? {
        portfolioService.get(userId, portfolioId)
        return valuationRepository.findTop1ByPortfolioIdOrderByAsOfDesc(portfolioId)
    }

    override fun history(
        userId: String,
        portfolioId: UUID,
        from: OffsetDateTime,
        to: OffsetDateTime,
    ): List<ValuationEntity> {
        portfolioService.get(userId, portfolioId)
        return valuationRepository.findAllByPortfolioIdAndAsOfBetween(portfolioId, from, to)
    }
}
