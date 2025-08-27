package com.notivest.portfolio_service.client.marketdata

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("pricefetcher")
data class PriceFetcherProps(
    val baseUrl: String,
    val timeoutMs: Long = 2000
)
