package com.notivest.portfolioservice.controller

import com.notivest.portfolioservice.models.HoldingEntity
import com.notivest.portfolioservice.models.HoldingMovementEntity
import com.notivest.portfolioservice.models.HoldingMovementType
import com.notivest.portfolioservice.models.portfolio.PortfolioEntity
import com.notivest.portfolioservice.repository.HoldingMovementRepository
import com.notivest.portfolioservice.repository.HoldingRepository
import com.notivest.portfolioservice.repository.PortfolioRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.math.BigDecimal
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("auth", "test")
class UserDataControllerIT {
    @Autowired lateinit var mockMvc: MockMvc

    @Autowired lateinit var portfolioRepository: PortfolioRepository

    @Autowired lateinit var holdingRepository: HoldingRepository

    @Autowired lateinit var holdingMovementRepository: HoldingMovementRepository

    @MockBean lateinit var jwtDecoder: JwtDecoder

    @BeforeEach
    fun setUp() {
        holdingMovementRepository.deleteAll()
        holdingRepository.deleteAll()
        portfolioRepository.deleteAll()
    }

    @Test
    fun `delete my user data removes only current user rows`() {
        val userOne = UUID.randomUUID()
        val userTwo = UUID.randomUUID()

        val userOnePortfolio = seedPortfolioData(userOne, "AAPL")
        val userTwoPortfolio = seedPortfolioData(userTwo, "MSFT")

        mockMvc
            .perform(
                delete("/v1/user-data/me")
                    .with(
                        jwt().jwt { token ->
                            token.claim("claim", userOne.toString())
                        },
                    ),
            ).andExpect(status().isNoContent)

        assertThat(portfolioRepository.findIdsByUserId(userOne)).isEmpty()
        assertThat(holdingRepository.countByPortfolioIdIn(listOf(userOnePortfolio))).isZero()
        assertThat(holdingMovementRepository.countByPortfolioIdIn(listOf(userOnePortfolio))).isZero()

        assertThat(portfolioRepository.findIdsByUserId(userTwo)).hasSize(1)
        assertThat(holdingRepository.countByPortfolioIdIn(listOf(userTwoPortfolio))).isEqualTo(1)
        assertThat(holdingMovementRepository.countByPortfolioIdIn(listOf(userTwoPortfolio))).isEqualTo(1)
    }

    @Test
    fun `delete my user data requires authentication`() {
        mockMvc
            .perform(delete("/v1/user-data/me"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `internal delete requires m2m scope`() {
        val userId = UUID.randomUUID()

        mockMvc
            .perform(
                delete("/internal/v1/user-data")
                    .queryParam("userId", userId.toString())
                    .with(jwt()),
            ).andExpect(status().isForbidden)

        mockMvc
            .perform(
                delete("/internal/v1/user-data")
                    .queryParam("userId", userId.toString())
                    .with(
                        jwt().authorities(
                            SimpleGrantedAuthority("SCOPE_portfolio:read:user-context"),
                        ),
                    ),
            ).andExpect(status().isNoContent)
    }

    private fun seedPortfolioData(
        userId: UUID,
        symbol: String,
    ): UUID {
        val portfolio =
            portfolioRepository.saveAndFlush(
                PortfolioEntity(
                    userId = userId,
                    name = "Main",
                    baseCurrency = "USD",
                ),
            )

        val savedHolding =
            holdingRepository.saveAndFlush(
                HoldingEntity(
                    portfolio = portfolio,
                    symbol = symbol,
                    quantity = BigDecimal("10"),
                    avgCost = BigDecimal("100"),
                ),
            )

        holdingMovementRepository.saveAndFlush(
            HoldingMovementEntity(
                portfolio = portfolio,
                holdingId = savedHolding.id,
                symbol = symbol,
                type = HoldingMovementType.BUY,
                quantity = BigDecimal("10"),
                price = BigDecimal("100"),
                note = "seed",
            ),
        )

        return requireNotNull(portfolio.id)
    }
}

