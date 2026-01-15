package com.notivest.portfolioservice.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.notivest.portfolioservice.dto.holding.response.HoldingSearchResponse
import com.notivest.portfolioservice.exception.NotFoundException
import com.notivest.portfolioservice.service.interfaces.UserHoldingsSearchService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Profile
import org.springframework.http.MediaType
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.security.web.SecurityFilterChain
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@ActiveProfiles("auth")
@WebMvcTest
@Import(
    InternalHoldingsControllerTest.TestSecurityConfig::class,
    InternalHoldingsControllerTest.TestControllerConfig::class,
    ApiExceptionHandler::class,
)
class InternalHoldingsControllerTest {
    @Autowired lateinit var mockMvc: MockMvc

    @Autowired lateinit var objectMapper: ObjectMapper

    @MockBean lateinit var userHoldingsSearchService: UserHoldingsSearchService

    @MockBean lateinit var jwtDecoder: JwtDecoder

    @Test
    fun `search returns holdings when scope present`() {
        val userId = UUID.randomUUID()
        val response =
            listOf(
                HoldingSearchResponse(
                    portfolioId = UUID.randomUUID(),
                    portfolioName = "Growth",
                    symbol = "AAPL",
                    quantity = BigDecimal("2.5"),
                    avgCost = BigDecimal("150.00"),
                    asOf = Instant.parse("2024-03-01T00:00:00Z"),
                    bookValue = BigDecimal("375.00"),
                    updatedAt = Instant.parse("2024-03-01T00:00:00Z"),
                ),
                HoldingSearchResponse(
                    portfolioId = UUID.randomUUID(),
                    portfolioName = "Income",
                    symbol = "MSFT",
                    quantity = BigDecimal("5.0"),
                    avgCost = BigDecimal("310.00"),
                    asOf = Instant.parse("2024-03-02T00:00:00Z"),
                    bookValue = BigDecimal("1550.00"),
                    updatedAt = Instant.parse("2024-03-02T00:00:00Z"),
                ),
            )

        whenever(userHoldingsSearchService.search(userId, listOf("AAPL", "MSFT")))
            .thenReturn(response)

        val payload =
            mapOf(
                "userId" to userId.toString(),
                "symbols" to listOf("AAPL", "MSFT"),
            )

        mockMvc
            .perform(
                post("/internal/v1/holdings/search")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(payload))
                    .with(jwt().authorities(SimpleGrantedAuthority("SCOPE_portfolio:read:user-context"))),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$[0].symbol").value("AAPL"))
            .andExpect(jsonPath("$[1].portfolioName").value("Income"))

        verify(userHoldingsSearchService).search(userId, listOf("AAPL", "MSFT"))
    }

    @Test
    fun `search returns 404 when service reports no holdings`() {
        val userId = UUID.randomUUID()
        whenever(userHoldingsSearchService.search(userId, listOf("TSLA")))
            .thenThrow(NotFoundException("No holdings found for requested symbols"))

        val payload =
            mapOf(
                "userId" to userId.toString(),
                "symbols" to listOf("TSLA"),
            )

        mockMvc
            .perform(
                post("/internal/v1/holdings/search")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(payload))
                    .with(jwt().authorities(SimpleGrantedAuthority("SCOPE_portfolio:read:user-context"))),
            ).andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found"))
    }

    @Test
    fun `search returns 400 when request validation fails`() {
        val payload =
            mapOf(
                "symbols" to listOf(" "),
            )

        mockMvc
            .perform(
                post("/internal/v1/holdings/search")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(payload))
                    .with(jwt().authorities(SimpleGrantedAuthority("SCOPE_portfolio:read:user-context"))),
            ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
    }

    @Test
    fun `search returns 403 when scope missing`() {
        val payload =
            mapOf(
                "userId" to UUID.randomUUID().toString(),
                "symbols" to listOf("AAPL"),
            )

        mockMvc
            .perform(
                post("/internal/v1/holdings/search")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(payload))
                    .with(jwt()),
            ).andExpect(status().isForbidden)
    }

    @Configuration
    @EnableWebSecurity
    @Profile("auth")
    class TestSecurityConfig {
        @Bean
        fun filterChain(http: HttpSecurity): SecurityFilterChain =
            http.csrf { it.disable() }
                .authorizeHttpRequests {
                    it.requestMatchers("/internal/v1/holdings/search").hasAuthority("SCOPE_portfolio:read:user-context")
                    it.anyRequest().authenticated()
                }
                .oauth2ResourceServer { it.jwt { } }
                .build()
    }

    @Configuration
    class TestControllerConfig {
        @Bean
        fun internalHoldingsController(userHoldingsSearchService: UserHoldingsSearchService): InternalHoldingsController =
            InternalHoldingsController(userHoldingsSearchService)
    }
}
