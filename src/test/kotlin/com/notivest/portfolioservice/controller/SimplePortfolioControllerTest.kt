package com.notivest.portfolioservice.controller

import com.notivest.portfolioservice.controller.PortfolioController
import com.notivest.portfolioservice.controller.dto.CreatePortfolioRequest
import com.notivest.portfolioservice.controller.dto.PortfolioResponse
import com.notivest.portfolioservice.models.PortfolioEntity
import com.notivest.portfolioservice.service.interfaces.PortfolioService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import java.security.Principal
import java.util.UUID

class SimplePortfolioControllerTest {
    private val portfolioService: PortfolioService = mock()
    private val controller = PortfolioController(portfolioService)
    private val principal: Principal =
        mock {
            on { name }.thenReturn("testuser")
        }

    @Test
    fun `should create portfolio successfully`() {
        val request = CreatePortfolioRequest("Test Portfolio", "USD")
        val portfolioId = UUID.randomUUID()
        val testPortfolio =
            PortfolioEntity(
                id = portfolioId,
                userId = "testuser",
                name = "Test Portfolio",
                baseCurrency = "USD",
            )

        whenever(portfolioService.create("testuser", "Test Portfolio", "USD"))
            .thenReturn(testPortfolio)

        val response = controller.create(request, principal)

        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertEquals(portfolioId, response.body?.id)
        assertEquals("Test Portfolio", response.body?.name)
        assertEquals("USD", response.body?.baseCurrency)
    }

    @Test
    fun `should get portfolio by id`() {
        val portfolioId = UUID.randomUUID()
        val testPortfolio =
            PortfolioEntity(
                id = portfolioId,
                userId = "testuser",
                name = "Test Portfolio",
                baseCurrency = "USD",
            )

        whenever(portfolioService.get("testuser", portfolioId))
            .thenReturn(testPortfolio)

        val response = controller.get(portfolioId, principal)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(portfolioId, response.body?.id)
        assertEquals("Test Portfolio", response.body?.name)
        assertEquals("USD", response.body?.baseCurrency)
    }
}
