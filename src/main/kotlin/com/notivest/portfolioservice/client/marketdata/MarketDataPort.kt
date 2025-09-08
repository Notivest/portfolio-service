package com.notivest.portfolioservice.client.marketdata

import java.math.BigDecimal

data class Quote(val symbol: String, val last: BigDecimal, val close: BigDecimal?, val ts: Long?)

interface MarketDataPort {
    fun quotes(symbols: List<String>): Map<String, Quote>

    /** Devuelve par→tasa, por ej: "EURUSD" -> 1.1065 */
    fun fx(pairs: List<String>): Map<String, BigDecimal>
}
