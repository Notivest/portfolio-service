package com.notivest.portfolioservice.config

import com.notivest.portfolioservice.client.marketdata.PriceFetcherProps
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
@EnableConfigurationProperties(PriceFetcherProps::class)
class RestClientConfig(private val props: PriceFetcherProps) {
    @Bean
    fun restClient(): RestClient =
        RestClient.builder()
            .baseUrl(props.baseUrl)
            .build()
}
