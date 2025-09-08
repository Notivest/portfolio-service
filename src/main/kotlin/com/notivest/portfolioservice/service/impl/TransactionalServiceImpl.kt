package com.notivest.portfolioservice.service.impl

import com.notivest.portfolioservice.controller.dto.TransactionDTO
import com.notivest.portfolioservice.controller.exception.ConflictException
import com.notivest.portfolioservice.models.TransactionEntity
import com.notivest.portfolioservice.models.enums.TransactionType
import com.notivest.portfolioservice.repository.TransactionRepository
import com.notivest.portfolioservice.service.interfaces.AccountService
import com.notivest.portfolioservice.service.interfaces.PortfolioService
import com.notivest.portfolioservice.service.interfaces.PositionService
import com.notivest.portfolioservice.service.interfaces.TransactionService
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

@Service
class TransactionalServiceImpl(
    private val transactionRepository: TransactionRepository,
    private val accountService: AccountService,
    private val portfolioService: PortfolioService,
    private val positionService: PositionService,
) : TransactionService {
    private val logger = LoggerFactory.getLogger(TransactionalServiceImpl::class.java)

    @Transactional
    override fun post(
        userId: String,
        portfolioId: UUID,
        req: TransactionDTO,
        idempotencyKey: String?,
    ): TransactionEntity {
        portfolioService.get(userId, portfolioId)
        val acc = accountService.get(userId, portfolioId, req.accountId)

        if (!idempotencyKey.isNullOrBlank() && transactionRepository.existsByIdempotencyKey(idempotencyKey)) {
            logger.warn("Idempotency key conflict for user=$userId, key=$idempotencyKey")
            throw ConflictException("Idempotency key already used: $idempotencyKey")
        }

        // Validaciones rápidas
        require(req.currency.length == 3) { "Invalid currency" }
        if (req.type == TransactionType.BUY || req.type == TransactionType.SELL) {
            require(!req.symbolId.isNullOrBlank()) { "symbolId is required" }
            require((req.qty ?: BigDecimal.ZERO) > BigDecimal.ZERO) { "qty > 0" }
            require((req.price ?: BigDecimal.ZERO) >= BigDecimal.ZERO) { "price >= 0" }
        }

        val entity =
            TransactionEntity(
                portfolioId = portfolioId,
                accountId = acc.id,
                symbolId = req.symbolId,
                type = req.type,
                qty = req.qty,
                price = req.price,
                fees = req.fees,
                taxes = req.taxes,
                currency = req.currency.uppercase(),
                tradeDate = req.tradeDate,
                settleDate = req.settleDate,
                fxRate = req.fxRate,
                note = req.note,
                source = req.source,
                idempotencyKey = idempotencyKey,
            )
        val saved = transactionRepository.save(entity)
        logger.info("Transaction created: id=${saved.id}, type=${saved.type}, symbol=${saved.symbolId}, qty=${saved.qty}, user=$userId")

        // Recalcular posición si aplica
        if (!saved.symbolId.isNullOrBlank() && (saved.type == TransactionType.BUY || saved.type == TransactionType.SELL)) {
            logger.debug("Recomputing position for symbol=${saved.symbolId}, account=${saved.accountId}")
            positionService.recomputeFor(userId, portfolioId, saved.accountId, saved.symbolId!!)
        }

        return saved
    }

    override fun get(
        userId: String,
        portfolioId: UUID,
        transactionId: UUID,
    ): TransactionEntity {
        portfolioService.get(userId, portfolioId)
        val transaction =
            transactionRepository.findById(transactionId)
                .orElseThrow { NoSuchElementException("Transaction not found") }
        require(transaction.portfolioId == portfolioId) { "Portfolio is not from that transaction" }
        return transaction
    }

    override fun search(
        userId: String,
        portfolioId: UUID,
        from: LocalDate?,
        to: LocalDate?,
        page: Int,
        size: Int,
    ): Page<TransactionEntity> {
        portfolioService.get(userId, portfolioId)
        val pageable = PageRequest.of(page, size, Sort.by("tradeDate").descending())
        return if (from != null && to != null) {
            transactionRepository.findAllByPortfolioIdAndTradeDateBetween(portfolioId, from, to, pageable)
        } else {
            // fallback simple
            val all = transactionRepository.findAllByPortfolioId(portfolioId).sortedByDescending { it.tradeDate }
            val start = page * size
            val content = all.drop(start).take(size)
            PageImpl(content, pageable, all.size.toLong())
        }
    }
}
