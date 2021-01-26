package com.seemsnerdy.trading

import java.math.BigDecimal


fun List<BigDecimal>.movingAverage(n: Int) =
    movingAverage(n) { window -> window.fold(BigDecimal.ZERO, { acc, item -> acc + item }) }

fun <K> List<K>.movingAverage(n: Int, sum: (List<K>) -> BigDecimal): List<MovingAverage<K>> {
    assert(n <= size)
    return this
        .windowed(size = n)
        .map { window ->
            val average = sum(window) / BigDecimal(window.size)
            MovingAverage(item = window.last(), value = average)
        }
}

enum class Trend {
    Up, Down
}

fun <K> List<MovingAverage<K>>.trend(): Trend {
    val window = takeLast(2)
    return if (window[0].value < window[1].value) {
        Trend.Up
    } else {
        Trend.Down
    }
}

fun <K> List<K>.exponentialMovingAverage(n: Int, item: (K) -> BigDecimal): List<MovingAverage<K>> {
    if (n >= size) {
        return emptyList()
    }
    val multiplier = BigDecimal(2f / (1 + n).toDouble())
    val first = take(n).fold(BigDecimal.ZERO, { acc, i -> acc + item(i) }) / BigDecimal(n)
    return drop(n)
        .lookingBack(MovingAverage(get(n - 1), first), { ma, i ->
            val newAverage = ((item(i) - ma.value) * multiplier) + ma.value
            MovingAverage(i, newAverage)
        })
}

private fun <K, T> List<K>.lookingBack(initial: T, f: (T, K) -> T): List<T> {
    var prev = initial
    return (0 until size)
        .map { i ->
            prev = f(prev, get(i))
            prev
        }
}

data class MovingAverage<T>(val item: T, val value: BigDecimal)