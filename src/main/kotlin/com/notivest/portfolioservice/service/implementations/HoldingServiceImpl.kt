package com.notivest.portfolioservice.service.implementations

import com.notivest.portfolioservice.dto.holding.applyTo
import com.notivest.portfolioservice.dto.holding.request.HoldingBuyRequest
import com.notivest.portfolioservice.dto.holding.request.HoldingCreateRequest
import com.notivest.portfolioservice.dto.holding.request.HoldingSellRequest
import com.notivest.portfolioservice.dto.holding.request.HoldingUpdateRequest
import com.notivest.portfolioservice.dto.holding.response.HoldingResponse
import com.notivest.portfolioservice.dto.holding.response.HoldingSummaryResponse
import com.notivest.portfolioservice.dto.holding.toEntity
import com.notivest.portfolioservice.dto.holding.toResponse
import com.notivest.portfolioservice.models.HoldingMovementEntity
import com.notivest.portfolioservice.models.HoldingMovementType
import com.notivest.portfolioservice.repository.HoldingMovementRepository
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
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale
import java.util.UUID

@Service
class HoldingServiceImpl(
    private val repo: HoldingRepository,
    private val portfolioRepo: PortfolioRepository,
    private val movementRepo: HoldingMovementRepository,
) : HoldingService {
    @Transactional(readOnly = true)
    override fun list(
        userId: UUID,
        portfolioId: UUID,
        symbolFilter: String?,
        pageable: Pageable,
    ): Page<HoldingResponse> {
        requirePortfolioOwned(userId, portfolioId)

        val page =
            if (symbolFilter.isNullOrBlank()) {
                repo.findAllByPortfolioId(portfolioId, pageable)
            } else {
                repo.findAllByPortfolioIdAndSymbolContainingIgnoreCase(
                    portfolioId,
                    symbolFilter.trim(),
                    pageable,
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

        val entity =
            normalized.toEntity(portfolio).also {
                // por si acaso, aseguramos uppercase
                it.symbol = it.symbol.uppercase(Locale.US)
            }

        try {
            val saved = repo.saveAndFlush(entity)
            return saved.toResponse()
        } catch (ex: DataIntegrityViolationException) {
            // Mapeamos la violación de UNIQUE (portfolio_id, symbol) a 409
            throw ResponseStatusException(HttpStatus.CONFLICT, "Holding for symbol already exists in this portfolio" + ex.message)
        }
    }

    @Transactional
    override fun buy(
        userId: UUID,
        portfolioId: UUID,
        req: HoldingBuyRequest,
    ): HoldingResponse {
        val portfolio = requirePortfolioOwned(userId, portfolioId)
        val symbol = normalizeSymbol(req.symbol)

        val existing = repo.findByPortfolioIdAndSymbolIgnoreCase(portfolioId, symbol)

        val holding =
            if (existing.isPresent) {
                val entity = existing.get()
                val oldQty = entity.quantity ?: BigDecimal.ZERO
                val oldAvg = entity.avgCost ?: BigDecimal.ZERO
                val buyQty = req.quantity
                val buyPrice = req.price

                val newQty = oldQty + buyQty
                val newAvg =
                    if (newQty > BigDecimal.ZERO) {
                        (oldQty * oldAvg + buyQty * buyPrice).divide(newQty, 8, RoundingMode.HALF_UP)
                    } else {
                        buyPrice
                    }

                entity.quantity = newQty
                entity.avgCost = newAvg
                if (req.note != null) {
                    entity.note = req.note
                }
                repo.saveAndFlush(entity)
            } else {
                val entity =
                    HoldingCreateRequest(
                        symbol = symbol,
                        quantity = req.quantity,
                        avgCost = req.price,
                        note = req.note,
                    ).toEntity(portfolio).also {
                        it.symbol = symbol
                    }

                try {
                    repo.saveAndFlush(entity)
                } catch (ex: DataIntegrityViolationException) {
                    // race condition with another buy/create
                    val again =
                        repo.findByPortfolioIdAndSymbolIgnoreCase(portfolioId, symbol)
                            .orElseThrow {
                                ResponseStatusException(
                                    HttpStatus.CONFLICT,
                                    "Holding for symbol '$symbol' already exists in portfolio '$portfolioId'",
                                )
                            }
                    again
                }
            }

        movementRepo.save(
            HoldingMovementEntity(
                portfolio = portfolio,
                holdingId = holding.id,
                symbol = symbol,
                type = HoldingMovementType.BUY,
                quantity = req.quantity,
                price = req.price,
                note = req.note,
            ),
        )

        return holding.toResponse()
    }

    @Transactional
    override fun sell(
        userId: UUID,
        portfolioId: UUID,
        req: HoldingSellRequest,
    ): HoldingResponse {
        val portfolio = requirePortfolioOwned(userId, portfolioId)
        val symbol = normalizeSymbol(req.symbol)

        val holding =
            repo.findByPortfolioIdAndSymbolIgnoreCase(portfolioId, symbol)
                .orElseThrow {
                    ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Holding not found for symbol '$symbol' in portfolio '$portfolioId'",
                    )
                }

        val oldQty = holding.quantity ?: BigDecimal.ZERO
        val sellQty = req.quantity

        if (sellQty > oldQty) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Cannot sell $sellQty of '$symbol': available quantity is $oldQty",
            )
        }

        val newQty = oldQty - sellQty

        if (req.note != null) {
            holding.note = req.note
        }

        val responseEntity =
            if (newQty.compareTo(BigDecimal.ZERO) == 0) {
                holding.quantity = BigDecimal.ZERO
                val saved = repo.saveAndFlush(holding)
                repo.delete(saved)
                saved
            } else {
                holding.quantity = newQty
                repo.saveAndFlush(holding)
            }

        movementRepo.save(
            HoldingMovementEntity(
                portfolio = portfolio,
                holdingId = responseEntity.id,
                symbol = symbol,
                type = HoldingMovementType.SELL,
                quantity = req.quantity,
                price = req.price,
                note = req.note,
            ),
        )

        return responseEntity.toResponse()
    }

    override fun update(
        userId: UUID,
        portfolioId: UUID,
        holdingId: UUID,
        req: HoldingUpdateRequest,
    ): HoldingResponse {
        requirePortfolioOwned(userId, portfolioId)

        val entity =
            repo.findByIdAndPortfolioId(holdingId, portfolioId)
                .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Holding not found") }

        // apply only non-null fields
        req.applyTo(entity)

        val saved = repo.saveAndFlush(entity)
        return saved.toResponse()
    }

    override fun delete(
        userId: UUID,
        portfolioId: UUID,
        holdingId: UUID,
    ) {
        requirePortfolioOwned(userId, portfolioId)

        val entity =
            repo.findByIdAndPortfolioId(holdingId, portfolioId)
                .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Holding not found") }

        repo.delete(entity)
    }

    @Transactional(readOnly = true)
    override fun getSummary(
        userId: UUID,
        portfolioId: UUID,
        limit: Int,
        sort: String,
    ): List<HoldingSummaryResponse> {
        requirePortfolioOwned(userId, portfolioId)

        val holdings = repo.findAllByPortfolioId(portfolioId, Pageable.unpaged()).content

        if (holdings.isEmpty()) {
            return emptyList()
        }

        val totalBookValue =
            holdings.sumOf { holding ->
                calculateBookValue(holding.quantity, holding.avgCost)
            }

        val sorted =
            when (sort) {
                "bookValue,desc" ->
                    holdings.sortedByDescending { holding ->
                        calculateBookValue(holding.quantity, holding.avgCost)
                    }
                "bookValue,asc" ->
                    holdings.sortedBy { holding ->
                        calculateBookValue(holding.quantity, holding.avgCost)
                    }
                "quantity,desc" -> holdings.sortedByDescending { it.quantity }
                "quantity,asc" -> holdings.sortedBy { it.quantity }
                "symbol,asc" -> holdings.sortedBy { it.symbol }
                "symbol,desc" -> holdings.sortedByDescending { it.symbol }
                else ->
                    holdings.sortedByDescending { holding ->
                        calculateBookValue(holding.quantity, holding.avgCost)
                    }
            }

        return sorted
            .take(limit)
            .map { holding ->
                val bookValue = calculateBookValue(holding.quantity, holding.avgCost)

                val weight =
                    if (totalBookValue > BigDecimal.ZERO) {
                        bookValue.divide(totalBookValue, 8, RoundingMode.HALF_UP)
                    } else {
                        BigDecimal.ZERO
                    }

                HoldingSummaryResponse(
                    id = requireNotNull(holding.id) { "HoldingEntity.id must not be null" },
                    symbol = holding.symbol,
                    quantity = holding.quantity ?: BigDecimal.ZERO,
                    avgCost = holding.avgCost ?: BigDecimal.ZERO,
                    bookValue = bookValue,
                    weight = weight,
                    asOf = requireNotNull(holding.updatedAt) { "HoldingEntity.updatedAt must not be null" },
                )
            }
    }

    private fun calculateBookValue(
        quantity: BigDecimal?,
        avgCost: BigDecimal?,
    ): BigDecimal {
        return if (quantity != null && avgCost != null) {
            quantity * avgCost
        } else {
            BigDecimal.ZERO
        }
    }

    private fun requirePortfolioOwned(
        userId: UUID,
        portfolioId: UUID,
    ) = portfolioRepo.findByIdAndUserIdAndDeletedAtIsNull(portfolioId, userId)
        .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Portfolio not found") }

    private fun normalizeSymbol(symbol: String): String = symbol.trim().uppercase(Locale.US)
}
