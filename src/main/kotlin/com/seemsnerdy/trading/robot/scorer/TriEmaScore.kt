package com.seemsnerdy.trading.robot.scorer

import java.math.BigDecimal
import java.util.*

class TriEmaScore<K : Comparable<K>, V : Comparable<V>>(
    short: Int = 5,
    long: Int = 13,
    trend: Int = 200,
    converter: (K) -> Pair<V, BigDecimal>,
    threshold: Int = 3
) : Scorer {

    private val shortEma = Ema(n = short, converter = converter, threshold = threshold)
    private val longEma = Ema(n = long, converter = converter, threshold = threshold)
    private val trendEma = Ema(n = trend, converter = converter, threshold = threshold)

    override val shouldBuy: Boolean
        get() = shortEma.trend() == Trend.Up
                && shortEma.relation(longEma) == Relation.CrossedUp
                && longEma.trend() == Trend.Up

    override val shouldSell: Boolean
        get() = trendEma.oneLast()?.let { tEma ->
            shortEma.oneLast()?.let { sEma ->
                longEma.oneLast()?.let { lEma ->
                    tEma > lEma || lEma > sEma
                } ?: true
            } ?: true
        } ?: true


    fun append(vararg value: K) {
        shortEma.append(TreeSet(value.asList()))
        longEma.append(TreeSet(value.asList()))
        trendEma.append(TreeSet(value.asList()))
    }

}
