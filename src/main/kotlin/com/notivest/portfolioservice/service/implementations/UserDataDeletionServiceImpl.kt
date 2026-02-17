package com.notivest.portfolioservice.service.implementations

import com.notivest.portfolioservice.repository.HoldingMovementRepository
import com.notivest.portfolioservice.repository.HoldingRepository
import com.notivest.portfolioservice.repository.PortfolioRepository
import com.notivest.portfolioservice.service.interfaces.UserDataDeletionService
import com.notivest.portfolioservice.service.interfaces.UserDataDeletionSummary
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class UserDataDeletionServiceImpl(
    private val portfolioRepository: PortfolioRepository,
    private val holdingRepository: HoldingRepository,
    private val holdingMovementRepository: HoldingMovementRepository,
) : UserDataDeletionService {
    private val logger = LoggerFactory.getLogger(UserDataDeletionServiceImpl::class.java)

    @Transactional
    override fun deleteUserData(userId: UUID): UserDataDeletionSummary {
        val portfolioIds = portfolioRepository.findIdsByUserId(userId)

        val deletedHoldingMovements =
            if (portfolioIds.isEmpty()) {
                0L
            } else {
                holdingMovementRepository.deleteByPortfolioIdIn(portfolioIds)
            }

        val deletedHoldings =
            if (portfolioIds.isEmpty()) {
                0L
            } else {
                holdingRepository.deleteByPortfolioIdIn(portfolioIds)
            }

        val deletedPortfolios = portfolioRepository.deleteByUserId(userId)

        logger.info(
            "user-data-delete completed service=portfolio userId={} deletedHoldingMovements={} deletedHoldings={} deletedPortfolios={}",
            userId,
            deletedHoldingMovements,
            deletedHoldings,
            deletedPortfolios,
        )

        return UserDataDeletionSummary(
            deletedHoldingMovements = deletedHoldingMovements,
            deletedHoldings = deletedHoldings,
            deletedPortfolios = deletedPortfolios,
        )
    }
}

