package com.seemsnerdy.trading.robot.scorer

import java.math.BigDecimal
import java.math.MathContext
import java.util.*

private data class Rs<K : Comparable<K>>(val key: K, val data: BigDecimal) : Comparable<Rs<K>> {
    override fun compareTo(other: Rs<K>): Int = key.compareTo(other.key)

}

class Rsi<K : Comparable<K>, V : Comparable<V>>(
    n: Int = 14,
    val converter: (K) -> Pair<V, BigDecimal>,
    val threshold: Int = 5,
    val upperGate: BigDecimal = BigDecimal(70),
    val lowerGate: BigDecimal = BigDecimal(30)
) : Scorer {

//    private var data = TreeMap<V, RsiValues>()
//    private var window = LinkedList<BigDecimal>()

    val data: TreeMap<V, BigDecimal>
        get() {
            val upper = upperEma.data
            val lower = lowerEma.data

            if (upper.size < threshold || lower.size < threshold) {
                return TreeMap()
            }
            val keys = upper.keys.reversed().take(threshold).reversed()
            val joined = keys.map { k ->
                val u = upper[k]!!
                val l = lower[k]!!
                if (0f == l.toFloat()) {
                    k to BigDecimal(100)
                } else {
                    val rs = u.divide(l, MathContext.DECIMAL128)
                    k to BigDecimal(100)
                        .subtract(
                            BigDecimal(100)
                                .divide(BigDecimal.ONE.add(rs, MathContext.DECIMAL128), MathContext.DECIMAL128),
                            MathContext.DECIMAL128
                        )
                }
            }.toMap()
            return TreeMap(joined)
        }

    private val innerConverter = { rs: Rs<V> -> rs.key to rs.data }

    private val upperEma = Smma(n = n, threshold = threshold, converter = innerConverter)
    private val lowerEma = Smma(n = n, threshold = threshold, converter = innerConverter)

    private var prev: K? = null

    val isWorking: Boolean
        get() = upperEma.isWorking && lowerEma.isWorking

    fun append(vararg value: K) {
        val ordered = TreeSet(value.toList())
        ordered.forEach { v ->
            val p = prev
            if (p != null) {
                val converderThis = converter(v)
                val convertedPrev = converter(p)
                val change = converderThis.second.subtract(convertedPrev.second, MathContext.DECIMAL128)
                when {
                    change > BigDecimal.ZERO -> {
                        upperEma.append(TreeSet(mutableListOf(Rs(converderThis.first, change))))
                        lowerEma.append(TreeSet(mutableListOf(Rs(converderThis.first, BigDecimal.ZERO))))
                    }
                    change < BigDecimal.ZERO -> {
                        upperEma.append(TreeSet(mutableListOf(Rs(converderThis.first, BigDecimal.ZERO))))
                        lowerEma.append(TreeSet(mutableListOf(Rs(converderThis.first, change.abs()))))
                    }
                    else -> {
                        upperEma.append(TreeSet(mutableListOf(Rs(converderThis.first, BigDecimal.ZERO))))
                        lowerEma.append(TreeSet(mutableListOf(Rs(converderThis.first, BigDecimal.ZERO))))
                    }
                }
            }
            prev = v
        }
    }

//    val isWorking: Boolean
//        get() = data.size == threshold
//
//    fun append(vararg value: K) {
//        val set = TreeSet(value.asList())
//        set.map(converter)
//            .filter { v -> data.isEmpty() || data.lastKey() < v.first }
//            .forEach { v ->
//                if (window.size == n) {
//                    val averages = window
//                        .windowed(2)
//                        .fold((BigDecimal.ZERO to BigDecimal.ZERO), { acc, i ->
//                            val change = i[1].subtract(i[0], MathContext.DECIMAL128)
//                            when {
//                                change > BigDecimal.ZERO -> {
//                                    acc.first.add(change, MathContext.DECIMAL128) to acc.second
//                                }
//                                change < BigDecimal.ZERO -> {
//                                    acc.first to acc.second.add(change, MathContext.DECIMAL128)
//                                }
//                                else -> {
//                                    acc
//                                }
//                            }
//                        })
//                        .let { p ->
//                            p.first.divide(BigDecimal(n), MathContext.DECIMAL128) to p.second.divide(
//                                BigDecimal(
//                                    n
//                                ), MathContext.DECIMAL128
//                            )
//                        }
//                    data[v.first] = RsiValues(h = averages.first, l = averages.second.abs())
//                }
//                window.add(v.second)
//                if (window.size > n) {
//                    window.removeAt(0)
//                }
//
//                while (data.size > threshold) {
//                    data.remove(data.firstKey())
//                }
//            }
//    }

    //    override val shouldSell: Boolean
//        get() = data.entries.windowed(2)
//            .any { (it[0].value.value >= upperGate && it[1].value.value < upperGate) }
//    override val shouldBuy: Boolean
//        get() = isWorking && data.entries.windowed(2)
//            .any { (it[0].value.value <= lowerGate && it[1].value.value > lowerGate) }.takeIf { it }?.let {
//                println("RSI: from ${data.firstKey()} to ${data.lastKey()}")
//                println(data.map { it.value.value.setScale(2, RoundingMode.HALF_UP) }.joinToString(separator = ","))
//                true
//            } ?: false
    override val shouldSell: Boolean
        get() = data.entries.windowed(2)
            .any { (it[0].value >= upperGate && it[1].value < upperGate) }
    override val shouldBuy: Boolean
        get() = isWorking && data.entries.windowed(2)
            .any { (it[0].value <= lowerGate && it[1].value > lowerGate) }
}

private data class RsiValues(val h: BigDecimal, val l: BigDecimal) {
    val value: BigDecimal
        get() = l.takeIf { it > BigDecimal.ZERO }?.let { l ->
            BigDecimal(100)
                .subtract(
                    BigDecimal(100)
                        .divide(
                            BigDecimal.ONE
                                .add(
                                    h.divide(l, MathContext.DECIMAL128),
                                    MathContext.DECIMAL128
                                ), MathContext.DECIMAL128
                        ), MathContext.DECIMAL128
                )
        } ?: BigDecimal(100)

}