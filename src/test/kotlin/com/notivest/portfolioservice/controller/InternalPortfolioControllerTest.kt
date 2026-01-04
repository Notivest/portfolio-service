package com.notivest.portfolioservice.controller

import com.notivest.portfolioservice.dto.holding.response.HoldingSummaryResponse
import com.notivest.portfolioservice.service.interfaces.HoldingService
import com.notivest.portfolioservice.service.interfaces.InternalPortfolioService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.security.web.SecurityFilterChain
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@ActiveProfiles("auth")
@WebMvcTest
@Import(
    InternalPortfolioControllerTest.TestSecurityConfig::class,
    InternalPortfolioControllerTest.TestControllerConfig::class,
    ApiExceptionHandler::class,
)
class InternalPortfolioControllerTest {
    @Autowired lateinit var mockMvc: MockMvc

    @MockBean lateinit var internalPortfolioService: InternalPortfolioService

    @MockBean lateinit var holdingService: HoldingService

    @MockBean lateinit var jwtDecoder: JwtDecoder

    @Test
    fun `getHoldingsSummary returns summary when scope present`() {
        val userId = UUID.randomUUID()
        val portfolioId = UUID.randomUUID()

        val response =
            listOf(
                HoldingSummaryResponse(
                    id = UUID.randomUUID(),
                    symbol = "AAPL",
                    quantity = BigDecimal("10.0"),
                    avgCost = BigDecimal("150.0"),
                    marketValue = BigDecimal("1500.0"),
                    weight = BigDecimal("1.0"),
                    asOf = Instant.parse("2024-03-01T00:00:00Z"),
                ),
            )

        whenever(holdingService.getSummary(userId, portfolioId, 10, "marketValue,desc"))
            .thenReturn(response)

        mockMvc
            .perform(
                get("/internal/v1/portfolios/$portfolioId/holdings/summary")
                    .queryParam("userId", userId.toString())
                    .queryParam("limit", "10")
                    .queryParam("sort", "marketValue,desc")
                    .accept(MediaType.APPLICATION_JSON)
                    .with(jwt().authorities(SimpleGrantedAuthority("SCOPE_portfolio:read:user-context"))),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$[0].symbol").value("AAPL"))

        verify(holdingService).getSummary(userId, portfolioId, 10, "marketValue,desc")
    }

    @Test
    fun `getHoldingsSummary returns 400 when limit invalid`() {
        val userId = UUID.randomUUID()
        val portfolioId = UUID.randomUUID()

        mockMvc
            .perform(
                get("/internal/v1/portfolios/$portfolioId/holdings/summary")
                    .queryParam("userId", userId.toString())
                    .queryParam("limit", "0")
                    .with(jwt().authorities(SimpleGrantedAuthority("SCOPE_portfolio:read:user-context"))),
            ).andExpect(status().isBadRequest)
    }

    @Test
    fun `getHoldingsSummary returns 403 when scope missing`() {
        val userId = UUID.randomUUID()
        val portfolioId = UUID.randomUUID()

        mockMvc
            .perform(
                get("/internal/v1/portfolios/$portfolioId/holdings/summary")
                    .queryParam("userId", userId.toString())
                    .with(jwt()),
            ).andExpect(status().isForbidden)
    }

    @Configuration
    @EnableWebSecurity
    class TestSecurityConfig {
        @Bean
        fun filterChain(http: HttpSecurity): SecurityFilterChain =
            http.csrf { it.disable() }
                .authorizeHttpRequests {
                    it.requestMatchers("/internal/v1/portfolios/**").hasAuthority("SCOPE_portfolio:read:user-context")
                    it.anyRequest().authenticated()
                }
                .oauth2ResourceServer { it.jwt { } }
                .build()
    }

    @Configuration
    class TestControllerConfig {
        @Bean
        fun internalPortfolioController(
            internalPortfolioService: InternalPortfolioService,
            holdingService: HoldingService,
        ): InternalPortfolioController = InternalPortfolioController(internalPortfolioService, holdingService)
    }
}
