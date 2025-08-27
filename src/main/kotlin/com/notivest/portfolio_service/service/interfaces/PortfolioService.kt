package com.notivest.portfolio_service.service.interfaces

import com.notivest.portfolio_service.models.PortfolioEntity
import org.springframework.data.domain.Page
import java.util.UUID

interface PortfolioService {
    fun create(userId : String, name : String, baseCurrency : String) : PortfolioEntity
    fun get(userId : String , portfolioId : UUID) : PortfolioEntity
    fun list(userId : String, page : Int , size : Int) : Page<PortfolioEntity>
    fun update(userId :String, portfolioId : UUID, name : String?, baseCurrency : String?) : PortfolioEntity
    fun delete(userId : String, portfolioId : UUID)
}