package com.notivest.portfolio_service.valuation

import com.notivest.portfolio_service.client.marketdata.MarketDataPort
import com.notivest.portfolio_service.client.marketdata.Quote
import com.notivest.portfolio_service.service.impl.PositionServiceImpl
import com.notivest.portfolio_service.valuation.models.PositionInput
import com.notivest.portfolio_service.valuation.models.PositionValuation
import com.notivest.portfolio_service.valuation.models.ValuationResult
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.math.RoundingMode

private const val SCALE = 8

class ValuationCalculator(
    private val marketData: MarketDataPort
) {

    private val logger: Logger = LoggerFactory.getLogger(ValuationCalculator::class.java)


    fun compute(inputs: List<PositionInput>, baseCurrency: String): ValuationResult {
        if (inputs.isEmpty()) {
            return ValuationResult(emptyList(), BigDecimal.ZERO.setScale(SCALE), emptyMap())
        }

        val base = baseCurrency.uppercase()

        // 1) Obtener quotes
        val symbols = inputs.map { it.symbolId }.distinct()
        val quotes: Map<String, Quote> = marketData.quotes(symbols)

        // 2) Obtener pares FX necesarios
        val pairs = inputs.map { "${it.currency.uppercase()}$base" }
            .filter { !it.startsWith(base) }
            .distinct()
        val fxMap = if (pairs.isNotEmpty()) marketData.fx(pairs) else emptyMap()

        // 3) Calcular por posición
        var nav = BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP)
        val valuations = inputs.map { p ->
            val px = quotes[p.symbolId]?.last ?: run {
                logger.warn("No hay quote para ${p.symbolId}, fallback a avgCost")
                p.avgCost
            }
            val pair = "${p.currency.uppercase()}$base"
            val rate = when {
                p.currency.equals(base, true) -> BigDecimal.ONE
                fxMap.containsKey(pair) -> fxMap.getValue(pair)
                else -> {
                    logger.warn("No hay FX para $pair, fallback 1.0")
                    BigDecimal.ONE
                }
            }

            val mtmBase = p.qty.multiply(px).multiply(rate).setScale(SCALE, RoundingMode.HALF_UP)
            val costBase = p.qty.multiply(p.avgCost).multiply(rate).setScale(SCALE, RoundingMode.HALF_UP)
            val unreal = mtmBase.subtract(costBase).setScale(SCALE, RoundingMode.HALF_UP)

            nav = nav.add(mtmBase)

            PositionValuation(
                symbolId = p.symbolId,
                qty = p.qty,
                avgCost = p.avgCost,
                lastPrice = px,
                currency = p.currency.uppercase(),
                fxToBase = rate,
                mtmBase = mtmBase,
                costBase = costBase,
                unrealizedPnL = unreal
            )
        }

        return ValuationResult(
            positions = valuations,
            nav = nav,
            fxUsed = fxMap
        )
    }
}