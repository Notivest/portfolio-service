package com.notivest.portfolioservice.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.notivest.portfolioservice.client.marketdata.MarketDataPort
import com.notivest.portfolioservice.valuation.ValuationCalculator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ValuationBeans(private val marketData: MarketDataPort) {
    @Bean
    fun valuationCalculator(): ValuationCalculator = ValuationCalculator(marketData)

    @Bean
    fun objectMapper(): ObjectMapper = ObjectMapper().registerModule(kotlinModule())
}
