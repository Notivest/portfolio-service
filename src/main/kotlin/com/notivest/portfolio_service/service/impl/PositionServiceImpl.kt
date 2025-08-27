package com.notivest.portfolio_service.service.impl

import com.notivest.portfolio_service.models.PositionEntity
import com.notivest.portfolio_service.models.enums.TransactionType
import com.notivest.portfolio_service.repository.PositionRepository
import com.notivest.portfolio_service.repository.TransactionRepository
import com.notivest.portfolio_service.service.interfaces.PortfolioService
import com.notivest.portfolio_service.service.interfaces.PositionService
import org.springframework.transaction.annotation.Transactional
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.OffsetDateTime
import java.util.UUID

@Service
class PositionServiceImpl(
    private val portfolioService: PortfolioService,
    private val transactionRepository: TransactionRepository,
    private val positionRepository: PositionRepository
) : PositionService {

    private val logger: Logger = LoggerFactory.getLogger(PositionServiceImpl::class.java)

    @Transactional
    override fun recomputeFor(
        userId: String,
        portfolioId: UUID,
        accountId: UUID,
        symbolId: String
    ): PositionEntity {
        portfolioService.get(userId, portfolioId)

        val txs = transactionRepository.findAllByPortfolioId(portfolioId)
            .filter { it.accountId == accountId && it.symbolId == symbolId }
            .sortedBy { it.tradeDate }

        var qty = BigDecimal.ZERO
        var avgCost = BigDecimal.ZERO

        for (tx in txs) {
            when (tx.type) {
                TransactionType.BUY -> {
                    val q = tx.qty ?: BigDecimal.ZERO
                    val p = tx.price ?: BigDecimal.ZERO
                    val total = p.multiply(q).add(tx.fees).add(tx.taxes)
                    val newQty = qty.add(q)
                    avgCost = if (newQty.signum() == 0) BigDecimal.ZERO
                    else (avgCost.multiply(qty).add(total)).divide(newQty, 8, RoundingMode.HALF_UP)
                    qty = newQty
                }

                TransactionType.SELL -> {
                    val q = tx.qty ?: BigDecimal.ZERO
                    qty = qty.subtract(q).max(BigDecimal.ZERO) // sin cortos por ahora
                    // avgCost se mantiene en PPP
                }

                else -> { logger.warn("Invalid transaction type: ${tx.type} for symbol: $symbolId") }
            }
        }

        val existing = positionRepository.findByPortfolioIdAndSymbolId(portfolioId, symbolId)
        val pos = (existing ?: PositionEntity(
            portfolioId = portfolioId, accountId = accountId, symbolId = symbolId
        )).apply {
            this.qty = qty
            this.avgCost = avgCost
            this.currency = existing?.currency ?: "USD"
            this.updatedAt = OffsetDateTime.now()
        }
        return positionRepository.save(pos)
    }

    override fun list(
        userId: String,
        portfolioId: UUID
    ): List<PositionEntity> {
        portfolioService.get(userId, portfolioId)
        return positionRepository.findAllByPortfolioId(portfolioId)
    }

    override fun getOne(
        userId: String,
        portfolioId: UUID,
        symbolId: String
    ): PositionEntity? {
        portfolioService.get(userId, portfolioId)
        return positionRepository.findByPortfolioIdAndSymbolId(portfolioId, symbolId)
    }
}