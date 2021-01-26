package com.seemsnerdy.trading.robot.scorer

import org.junit.Test
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import kotlin.test.assertEquals

internal class EmaKtTest {

    data class TestPair(val key: Int, val value: BigDecimal) : Comparable<TestPair> {
        override fun compareTo(other: TestPair): Int = key.compareTo(other.key)

    }

    @Test
    fun emaTest() {
        val n = 4
        val ema = Ema(n = n, converter = { p: TestPair -> p.key to p.value }, threshold = 1)
        val values = TreeSet(
            listOf(
                TestPair(1, BigDecimal(3)),
                TestPair(2, BigDecimal(4)),
                TestPair(3, BigDecimal(2)),
                TestPair(4, BigDecimal(8)),
                TestPair(5, BigDecimal(7))
            )
        )
        ema.append(values)
        assertEquals(
            ema.oneLast()?.setScale(2, RoundingMode.HALF_UP),
            BigDecimal(5.35).setScale(2, RoundingMode.HALF_UP)
        )
    }

    @Test
    fun rsiTest() {
        val n = 14
        val rsi = Rsi(n = n, converter = { p: TestPair -> p.key to p.value }, threshold = 1)
        rsi.append(
            TestPair(1, BigDecimal(44.34)),
            TestPair(2, BigDecimal(44.09)),
            TestPair(3, BigDecimal(44.15)),
            TestPair(4, BigDecimal(43.61)),
            TestPair(5, BigDecimal(44.33)),
            TestPair(6, BigDecimal(44.83)),
            TestPair(7, BigDecimal(45.10)),
            TestPair(8, BigDecimal(45.42)),
            TestPair(9, BigDecimal(45.84)),
            TestPair(10, BigDecimal(46.08)),
            TestPair(11, BigDecimal(45.89)),
            TestPair(12, BigDecimal(46.03)),
            TestPair(13, BigDecimal(45.61)),
            TestPair(14, BigDecimal(46.28)),
            TestPair(15, BigDecimal(46.28)),
            TestPair(16, BigDecimal(46.00)),
            TestPair(17, BigDecimal(46.03)),
            TestPair(18, BigDecimal(46.41)),
            TestPair(19, BigDecimal(46.22)),
            TestPair(20, BigDecimal(45.64)),
            TestPair(21, BigDecimal(46.21)),
            TestPair(22, BigDecimal(46.25)),
            TestPair(23, BigDecimal(45.71)),
            TestPair(24, BigDecimal(46.45)),
            TestPair(25, BigDecimal(45.78)),
            TestPair(26, BigDecimal(45.35)),
            TestPair(27, BigDecimal(44.03)),
            TestPair(28, BigDecimal(44.18)),
            TestPair(29, BigDecimal(44.22)),
            TestPair(30, BigDecimal(44.57)),
            TestPair(31, BigDecimal(43.42)),
            TestPair(32, BigDecimal(42.66)),
            TestPair(33, BigDecimal(43.13)),
        )
        println(rsi.data)
    }
}