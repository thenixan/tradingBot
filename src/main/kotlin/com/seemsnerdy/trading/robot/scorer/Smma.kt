package com.seemsnerdy.trading.robot.scorer

import java.math.BigDecimal
import java.math.MathContext

class Smma<K : Comparable<K>, V : Comparable<V>>(
    private val n: Int,
    converter: (K) -> Pair<V, BigDecimal>,
    threshold: Int = 3
) : Ema<K, V>(n, converter, threshold) {

    override val multiplier: BigDecimal
        get() = BigDecimal(1).divide(BigDecimal(n), MathContext.DECIMAL128)
}