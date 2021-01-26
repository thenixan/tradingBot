package com.seemsnerdy.trading.robot

import com.seemsnerdy.trading.api.RestApi
import com.seemsnerdy.trading.api.objects.Currency
import com.seemsnerdy.trading.api.objects.InstrumentType
import com.seemsnerdy.trading.api.objects.responses.Candle
import com.seemsnerdy.trading.api.objects.responses.CandleResolution
import com.seemsnerdy.trading.api.objects.responses.MarketInstrument
import com.seemsnerdy.trading.api.objects.responses.PortfolioPosition
import io.reactivex.rxjava3.core.Maybe
import retrofit2.HttpException
import java.math.BigDecimal
import java.util.*

class Robot(val api: RestApi) {

    private val scores = mutableMapOf<String, Scorer>()

    fun getStocks(): List<MarketInstrument> =
        api.marketStocks()
            .map { it.payload.instruments.filter { i -> i.type == InstrumentType.Stock } }
            .blockingGet()


    fun getScoring(figi: String, ticker: String): Maybe<Scorer> = Maybe.create { emitter ->
        var score = scores[ticker] ?: Scorer()
        val lastUpdate = score.lastUpdate
        if (!score.isWorking || lastUpdate == null) {
            val candles = mutableListOf<Candle>()
            var i = 0
            while (!score.isWorking && i < 10) {
                val from = Calendar.getInstance().also { it.add(Calendar.HOUR, -24 + (-24 * i)) }.time
                val to = Calendar.getInstance().also { it.add(Calendar.HOUR, -24 * i) }.time
                val response = api
                    .marketCandles(
                        figi = figi,
                        from = from,
                        to = to,
                        interval = CandleResolution.OneMin
                    )
                    .toMaybe()
                    .retry { i, t ->
                        println("Retrying $ticker candles the $i time: $t")
                        t !is HttpException
                    }
                    .onErrorComplete()
                    .blockingGet()
                if (response == null) {
                    emitter.onComplete()
                    return@create
                }
                i += 1
                candles.addAll(response.payload.candles)
                score = Scorer().also { it.append(*candles.toTypedArray()) }
            }
            scores[ticker] = score
        } else {
            val candles = api.marketCandles(
                figi = figi,
                from = lastUpdate,
                to = Calendar.getInstance().time,
                interval = CandleResolution.OneMin
            )
                .toMaybe()
                .retry { i, t ->
                    println("Retrying $ticker candles the $i time: $t")
                    t !is HttpException
                }
                .onErrorComplete()
                .blockingGet()
                .payload
                .candles
            score.append(*candles.toTypedArray())
        }
        emitter.onSuccess(score)
    }


    fun runPortfolioScoring(): List<NewScore> =
        api.portfolio()
            .retry { i, t ->
                println("Retrying portfolio list the $i'th time: $t")
                t !is HttpException
            }
            .map { response ->
                response
                    .payload
                    .positions
                    .filter { i -> i.instrumentType == InstrumentType.Stock }
            }
            .flattenAsFlowable { it }
            .flatMapMaybe { stock ->
                getScoring(stock.figi, stock.ticker)
                    .map { NewScore(stock, it) }
                    .filter { it.score.isWorking }
            }
            .toList()
            .blockingGet()


    fun stocksBalances(): Map<Currency?, BigDecimal> =
        api.portfolio()
            .retry { i, t ->
                println("Retrying portfolio the $i'th time: $t")
                t !is HttpException
            }
            .blockingGet()
            .payload
            .positions
            .filterNot { it.instrumentType == InstrumentType.Currency }
            .groupingBy { it.averagePositionPrice?.currency }
            .fold(
                BigDecimal.ZERO,
                { total, stock ->
                    total + (stock.averagePositionPrice?.value
                        ?: BigDecimal.ZERO * stock.balance) - stock.expectedYield.value
                }
            )

    fun balances(): Map<Currency, BigDecimal> =
        api.portfolioCurrencies()
            .retry { i, t ->
                println("Retrying balances the $i'th time: $t")
                t !is HttpException
            }
            .blockingGet()
            .payload
            .currencies
            .groupingBy { it.currency }
            .fold(BigDecimal.ZERO, { total, c -> total + c.balance })
}

data class NewScore(val portfolioPosition: PortfolioPosition, val score: Scorer)