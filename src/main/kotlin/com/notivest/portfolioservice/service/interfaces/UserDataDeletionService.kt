package com.notivest.portfolioservice.service.interfaces

import java.util.UUID

data class UserDataDeletionSummary(
    val deletedHoldingMovements: Long,
    val deletedHoldings: Long,
    val deletedPortfolios: Long,
)

interface UserDataDeletionService {
    fun deleteUserData(userId: UUID): UserDataDeletionSummary
}

