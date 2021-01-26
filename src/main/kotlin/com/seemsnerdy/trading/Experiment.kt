package com.seemsnerdy.trading

import com.seemsnerdy.trading.api.objects.responses.Candle
import com.seemsnerdy.trading.api.objects.responses.CandleResolution
import com.seemsnerdy.trading.api.objects.responses.MarketInstrument
import com.seemsnerdy.trading.robot.Robot
import com.seemsnerdy.trading.robot.scorer.Rsi
import com.seemsnerdy.trading.robot.scorer.TriEmaScore
import com.seemsnerdy.trading.robot.scorer.TwoEmaScore
import java.math.BigDecimal
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

object Experiment {

    fun run(
        robot: Robot,
        stock: MarketInstrument,
        startingBalance: BigDecimal,
        startingQuantity: BigDecimal
    ): String {

        val stock = robot.api.searchByTicker(ticker = "BBSI").blockingGet().payload.instruments.first()

        val candles = (0 until 3)
            .reversed()
            .map { i ->
                Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -i)
                }
            }
            .flatMap { d ->
                val from = d.also {
                    it.set(Calendar.HOUR_OF_DAY, 0)
                    it.set(Calendar.MINUTE, 0)
                    it.set(Calendar.SECOND, 0)
                }.time
                val to = d.also {
                    it.set(Calendar.HOUR_OF_DAY, 23)
                    it.set(Calendar.MINUTE, 59)
                    it.set(Calendar.SECOND, 59)
                }.time
                robot
                    .api
                    .marketCandles(
                        figi = stock.figi,
                        from = from,
                        to = to,
                        interval = CandleResolution.OneMin
                    )
                    .blockingGet()
                    .payload
                    .candles
            }

        println("OLD")
        val bOld = runOld(stock, candles, startingBalance, startingQuantity)
        println("NEW")
        val bNew = runNew(stock, candles, startingBalance, startingQuantity)
        return "Balance: ${bOld.setScale(2)} vs ${bNew.setScale(2)}"
    }

    private fun runOld(
        stock: MarketInstrument,
        candles: List<Candle>,
        startingBalance: BigDecimal,
        startingQuantity: BigDecimal
    ): BigDecimal {
        var balance = startingBalance
        var quantity = startingQuantity
        var purchasePrice = BigDecimal.ZERO

        val ema = TwoEmaScore(converter = { c: Candle -> c.time to c.c })
        val rsi = Rsi(converter = { c: Candle -> c.time to c.c })
        candles.forEach { c ->
            ema.append(c)
            rsi.append(c)
            if (quantity > BigDecimal.ZERO && (ema.shouldSell || rsi.shouldSell) && purchasePrice < c.c) {
                println(
                    "Sold at ${
                        SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(c.time)
                    }"
                )
                balance += quantity * c.c
                quantity = BigDecimal.ZERO
            } else if (ema.shouldBuy && rsi.shouldBuy && balance >= c.c) {
                println(
                    "Purchased at ${
                        SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(c.time)
                    }"
                )
                balance -= c.c
                purchasePrice = ((purchasePrice * quantity) + c.c) / (quantity + BigDecimal.ONE)
                quantity += BigDecimal.ONE
            }
        }

        return balance + (quantity * candles.last().c)
    }

    private fun runNew(
        stock: MarketInstrument,
        candles: List<Candle>,
        startingBalance: BigDecimal,
        startingQuantity: BigDecimal
    ): BigDecimal {
        var balance = startingBalance
        var quantity = startingQuantity
        var purchasePrice = BigDecimal.ZERO

        val ema = TriEmaScore(converter = { c: Candle -> c.time to c.c })
        candles.forEach { c ->
            ema.append(c)
            if (quantity > BigDecimal.ZERO && ema.shouldSell && purchasePrice < c.c) {
                println(
                    "Sold at ${
                        SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(c.time)
                    }"
                )
                balance += quantity * c.c
                quantity = BigDecimal.ZERO
            } else if (ema.shouldBuy && balance >= c.c) {
                println(
                    "Purchased at ${
                        SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(c.time)
                    }"
                )
                balance -= c.c
                purchasePrice = ((purchasePrice * quantity) + c.c) / (quantity + BigDecimal.ONE)
                quantity += BigDecimal.ONE
            }
        }
        return balance + (quantity * candles.last().c)
    }
}