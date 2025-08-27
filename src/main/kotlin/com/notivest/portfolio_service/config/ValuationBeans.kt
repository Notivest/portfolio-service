package com.notivest.portfolio_service.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.notivest.portfolio_service.client.marketdata.MarketDataPort
import com.notivest.portfolio_service.valuation.ValuationCalculator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ValuationBeans(private val marketData: MarketDataPort) {

    @Bean
    fun valuationCalculator(): ValuationCalculator =
        ValuationCalculator(marketData)

    @Bean
    fun objectMapper(): ObjectMapper =
        ObjectMapper().registerModule(KotlinModule())
}