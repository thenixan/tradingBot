package com.seemsnerdy.trading.robot.scorer

import java.math.BigDecimal
import java.math.MathContext
import java.util.*

open class Ema<K : Comparable<K>, V : Comparable<V>>(
    private val n: Int,
    private val converter: (K) -> Pair<V, BigDecimal>,
    private val threshold: Int = 3
) {

    open val multiplier: BigDecimal =
        BigDecimal(2).divide(BigDecimal.ONE.add(BigDecimal(n), MathContext.DECIMAL128), MathContext.DECIMAL128)

    var data = TreeMap<V, BigDecimal>()

    private var average = Average(n)

    val isWorking
        get() = data.size == threshold + n

    fun oneLast(): BigDecimal? = data.lastEntry()?.value

    fun last(count: Int): List<BigDecimal>? =
        data.entries
            .reversed()
            .take(count)
            .takeIf { it.size == count }
            ?.reversed()
            ?.map { it.value }

    fun relation(other: Ema<K, V>): Relation {
        val thisLast = last(3) ?: return Relation.Undefined
        val otherLast = other.last(3) ?: return Relation.Undefined
        return if (thisLast[0] < otherLast[1] && thisLast[1] < otherLast[1] && thisLast[2] < otherLast[2]) {
            Relation.Lower
        } else if (thisLast[0] > otherLast[1] && thisLast[1] > otherLast[1] && thisLast[2] > otherLast[2]) {
            Relation.Higher
        } else if (thisLast[0] < otherLast[1] && thisLast[2] > otherLast[2]) {
            Relation.CrossedUp
        } else if (thisLast[0] > otherLast[1] && thisLast[2] < otherLast[2]) {
            Relation.CrossedDown
        } else if (thisLast[0] > otherLast[1] && thisLast[1] <= otherLast[1] && thisLast[2] > otherLast[2]) {
            Relation.BouncedDownUp
        } else if (thisLast[0] < otherLast[1] && thisLast[1] >= otherLast[1] && thisLast[2] < otherLast[2]) {
            Relation.BouncedUpDown
        } else {
            Relation.Undefined
        }
    }

    fun trend(): Trend {
        val last = last(3) ?: return Trend.Undefined
        return if (last[0] < last[1] && last[1] < last[2]) {
            Trend.Up
        } else if (last[0] > last[1] && last[1] > last[2]) {
            Trend.Down
        } else {
            Trend.Undefined
        }
    }

    fun append(values: TreeSet<K>) {
        if (values.isEmpty()) {
            return
        }
        val first = values.first()
        val converted = converter(first)
        if (data.isEmpty()) {
            average += converted.second
            if (average.isReady) {
                data[converted.first] = average.value
            }
        } else if (converted.first > data.lastKey()) {
            val last = data.lastEntry().value
            val newValue =
                converted.second.subtract(last, MathContext.DECIMAL128).multiply(multiplier, MathContext.DECIMAL128)
                    .add(last, MathContext.DECIMAL128)
            data[converted.first] = newValue
        }
        while (data.size > n + threshold) {
            data.remove(data.firstKey())
        }
        append(TreeSet(values.drop(1)))
    }

}

enum class Relation {
    CrossedUp,
    CrossedDown,
    Lower,
    Higher,
    BouncedUpDown,
    BouncedDownUp,
    Undefined
}

enum class Trend {
    Up,
    Down,
    Undefined
}