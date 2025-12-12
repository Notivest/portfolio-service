package com.notivest.portfolioservice.service.implementations

import com.notivest.portfolioservice.dto.holding.response.HoldingSearchResponse
import com.notivest.portfolioservice.exception.NotFoundException
import com.notivest.portfolioservice.repository.HoldingRepository
import com.notivest.portfolioservice.service.interfaces.UserHoldingsSearchService
import jakarta.validation.ConstraintViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.util.Locale
import java.util.UUID

@Service
class UserHoldingsSearchServiceImpl(
    private val holdingRepository: HoldingRepository,
) : UserHoldingsSearchService {
    @Transactional(readOnly = true)
    override fun search(
        userId: UUID,
        symbols: List<String>,
    ): List<HoldingSearchResponse> {
        if (symbols.isEmpty()) {
            throw ConstraintViolationException("symbols must contain between 1 and 20 entries", emptySet())
        }
        if (symbols.size > 20) {
            throw ConstraintViolationException("symbols must contain between 1 and 20 entries", emptySet())
        }

        val normalizedSymbols =
            symbols.mapIndexed { index, raw ->
                val trimmed = raw.trim()
                if (trimmed.isEmpty()) {
                    throw ConstraintViolationException("symbols[$index] must not be blank", emptySet())
                }
                if (trimmed.length > 15) {
                    throw ConstraintViolationException("symbols[$index] length must be at most 15 characters", emptySet())
                }
                trimmed.uppercase(Locale.US)
            }.distinct()

        if (normalizedSymbols.isEmpty()) {
            throw ConstraintViolationException("symbols must contain at least one value", emptySet())
        }

        val holdings = holdingRepository.findByUserIdAndSymbols(userId, normalizedSymbols)
        if (holdings.isEmpty()) {
            throw NotFoundException("No holdings found for requested symbols")
        }

        return holdings.map { holding ->
            val portfolio = holding.portfolio
            val portfolioId =
                portfolio.id
                    ?: throw IllegalStateException("Holding ${holding.id} is linked to a portfolio without id")
            val updatedAt =
                holding.updatedAt
                    ?: throw IllegalStateException("Holding ${holding.id} is missing updatedAt")

            val quantity = holding.quantity
            val avgCost = holding.avgCost
            val marketValue =
                if (quantity != null && avgCost != null) {
                    quantity * avgCost
                } else {
                    BigDecimal.ZERO
                }

            HoldingSearchResponse(
                portfolioId = portfolioId,
                portfolioName = portfolio.name,
                symbol = holding.symbol.uppercase(Locale.US),
                quantity = holding.quantity,
                avgCost = holding.avgCost,
                asOf = updatedAt,
                marketValue = marketValue,
                updatedAt = updatedAt,
            )
        }
    }
}
