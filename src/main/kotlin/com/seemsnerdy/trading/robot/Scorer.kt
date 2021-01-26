package com.seemsnerdy.trading.robot

import com.seemsnerdy.trading.api.objects.responses.Candle
import com.seemsnerdy.trading.robot.scorer.Rsi
import com.seemsnerdy.trading.robot.scorer.TwoEmaScore
import java.math.RoundingMode
import java.util.*

class Scorer {

    private val ema = TwoEmaScore(converter = { c: Candle -> c.time to c.c })
    private val rsi = Rsi(converter = { c: Candle -> c.time to c.c })

    val shouldSell: Boolean
        get() = ema.shouldSell || rsi.shouldSell

    val shouldBuy: Boolean
        get() = ema.shouldBuy && rsi.shouldBuy

    fun printRsi(name: String) =
        println("$name: ${rsi.data.map { it.value.setScale(2, RoundingMode.HALF_UP) }.joinToString(separator = ", ")}")

    val isWorking
        get() = ema.isWorking && rsi.isWorking

    private var lastCandle: Candle? = null
    val lastUpdate: Date?
        get() = lastCandle?.time

    fun append(vararg c: Candle) {
        if (c.isEmpty()) {
            return
        }
        val newLastCandle = c.maxByOrNull { it.time }
        newLastCandle?.let {
            if (lastCandle?.time?.let { t -> t > it.time } != true) {
                lastCandle = it
            }
        }
        ema.append(*c)
        rsi.append(*c)
    }
}