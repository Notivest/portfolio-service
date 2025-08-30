package com.notivest.portfolioservice.service.impl

import com.notivest.portfolioservice.models.PortfolioEntity
import com.notivest.portfolioservice.repository.PortfolioRepository
import com.notivest.portfolioservice.service.interfaces.PortfolioService
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.UUID

@Service
class PortfolioServiceImpl(
    private val portfolioRepository: PortfolioRepository,
) : PortfolioService {
    override fun create(
        userId: String,
        name: String,
        baseCurrency: String,
    ): PortfolioEntity {
        require(baseCurrency.length == 3) { "Invalid base currency" }
        val entity = PortfolioEntity(userId = userId, name = name, baseCurrency = baseCurrency.uppercase())
        return portfolioRepository.save(entity)
    }

    override fun get(
        userId: String,
        portfolioId: UUID,
    ): PortfolioEntity {
        return portfolioRepository.findById(portfolioId)
            .filter { it.userId == userId }
            .orElseThrow { NoSuchElementException("Portfolio not found") }
    }

    override fun list(
        userId: String,
        page: Int,
        size: Int,
    ): Page<PortfolioEntity> {
        return portfolioRepository.findAllByUserId(userId, PageRequest.of(page, size))
    }

    override fun update(
        userId: String,
        portfolioId: UUID,
        name: String?,
        baseCurrency: String?,
    ): PortfolioEntity {
        val p = get(userId, portfolioId)
        name?.let { p.name = it }
        baseCurrency?.let { p.baseCurrency = it.uppercase() }
        p.updatedAt = OffsetDateTime.now()
        return portfolioRepository.save(p)
    }

    override fun delete(
        userId: String,
        portfolioId: UUID,
    ) {
        val p = get(userId, portfolioId)
        portfolioRepository.delete(p)
    }
}
