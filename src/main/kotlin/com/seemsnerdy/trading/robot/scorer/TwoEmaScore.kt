package com.seemsnerdy.trading.robot.scorer

import java.math.BigDecimal
import java.util.*

class TwoEmaScore<K : Comparable<K>, V : Comparable<V>>(
    short: Int = 5,
    long: Int = 13,
    converter: (K) -> Pair<V, BigDecimal>,
    threshold: Int = 3
) : Scorer {

    private val shortEma = Ema(n = short, converter = converter, threshold = threshold)
    private val longEma = Ema(n = long, converter = converter, threshold = threshold)

    val isWorking
        get() = shortEma.isWorking && longEma.isWorking

    override val shouldBuy: Boolean
        get() = shortEma.relation(longEma) == Relation.CrossedUp

    override val shouldSell: Boolean
        get() = !listOf(Relation.CrossedUp, Relation.Higher).contains(shortEma.relation(longEma))


    fun append(vararg value: K) {
        val set = TreeSet(value.asList())
        shortEma.append(set)
        longEma.append(set)
    }

}