package com.seemsnerdy.trading.robot.scorer

import java.math.BigDecimal
import java.math.MathContext

class Average(private val n: Int, private val count: Int = 0, private val sum: BigDecimal = BigDecimal.ZERO) {

    val value: BigDecimal
        get() = sum.divide(BigDecimal(count), MathContext.DECIMAL128)

    val isReady
        get() = n == count

    operator fun plus(other: BigDecimal): Average =
        Average(n = n, count = count + 1, sum = sum.add(other, MathContext.DECIMAL128))

}