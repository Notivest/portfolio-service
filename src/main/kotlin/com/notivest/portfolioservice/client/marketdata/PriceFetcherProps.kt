package com.notivest.portfolioservice.client.marketdata

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("pricefetcher")
data class PriceFetcherProps(
    val baseUrl: String = "http://localhost:8080",
    val timeoutMs: Long = 2000,
)
