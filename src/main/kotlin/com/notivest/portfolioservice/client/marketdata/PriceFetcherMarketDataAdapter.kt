package com.notivest.portfolioservice.client.marketdata

import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpStatusCode
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import java.math.BigDecimal

@Component
class PriceFetcherMarketDataAdapter(
    private val client: RestClient,
) : MarketDataPort {
    @Suppress("UNCHECKED_CAST")
    override fun quotes(symbols: List<String>): Map<String, Quote> {
        if (symbols.isEmpty()) return emptyMap()
        val q = symbols.joinToString(",")

        val typeRef = object : ParameterizedTypeReference<Map<String, Map<String, Any?>>>() {}
        val body: Map<String, Map<String, Any?>> =
            client.get()
                .uri { it.path("/v1/quotes").queryParam("symbols", q).build() }
                .retrieve()
                .onStatus({ status: HttpStatusCode -> status.isError }) { _, r ->
                    error("PriceFetcher /quotes error: ${r.statusCode}")
                }
                .body(typeRef) ?: emptyMap()

        return body.mapValues { (sym, m) ->
            val lastNum = m["last"] as? Number ?: error("Campo 'last' ausente para $sym")
            val closeNum = m["close"] as? Number
            val tsNum = m["ts"] as? Number

            Quote(
                symbol = sym,
                last = BigDecimal.valueOf(lastNum.toDouble()),
                close = closeNum?.let { BigDecimal.valueOf(it.toDouble()) },
                ts = tsNum?.toLong(),
            )
        }
    }

    override fun fx(pairs: List<String>): Map<String, BigDecimal> {
        if (pairs.isEmpty()) return emptyMap()
        val q = pairs.joinToString(",")

        val typeRef = object : ParameterizedTypeReference<Map<String, Number>>() {}
        val body: Map<String, Number> =
            client.get()
                .uri { it.path("/v1/fx").queryParam("pairs", q).build() }
                .retrieve()
                .onStatus({ status: HttpStatusCode -> status.isError }) { _, r ->
                    error("PriceFetcher /fx error: ${r.statusCode}")
                }
                .body(typeRef) ?: emptyMap()

        return body.mapValues { (_, n) -> BigDecimal.valueOf(n.toDouble()) }
    }
}
