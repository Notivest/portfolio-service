package com.notivest.portfolioservice.service.integration

import com.notivest.portfolioservice.dto.portfolio.request.PortfolioCreateRequest
import com.notivest.portfolioservice.repository.PortfolioRepository
import com.notivest.portfolioservice.service.interfaces.PortfolioService
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.util.UUID

@SpringBootTest
@ActiveProfiles("test") // tu H2 + flyway
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PortfolioServiceIT {

    @Autowired
    lateinit var service: PortfolioService
    @Autowired
    lateinit var portfolioRepository: PortfolioRepository

    @Test
    fun `create+get end-to-end works against H2`() {
        val userId = UUID.randomUUID()
        val created = service.create(userId, PortfolioCreateRequest("Main", "USD"))
        val found = service.get(userId, created.id)
        Assertions.assertThat(found.name).isEqualTo("Main")
    }
}