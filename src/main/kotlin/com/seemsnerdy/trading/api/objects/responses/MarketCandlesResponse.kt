package com.seemsnerdy.trading.api.objects.responses

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal
import java.util.*

data class MarketCandlesResponse(
    @SerializedName("trackingId") override val trackingId: String,
    @SerializedName("status") override val status: String,
    @SerializedName("payload") val payload: Candles
) : Response()

data class Candles(
    @SerializedName("figi") val figi: String,
    @SerializedName("interval") val interval: CandleResolution,
    @SerializedName("candles") val candles: List<Candle>,
)

data class Candle(
    @SerializedName("figi") val figi: String,
    @SerializedName("interval") val interval: CandleResolution,
    @SerializedName("o") val o: BigDecimal,
    @SerializedName("c") val c: BigDecimal,
    @SerializedName("h") val h: BigDecimal,
    @SerializedName("l") val l: BigDecimal,
    @SerializedName("v") val v: Int,
    @SerializedName("time") val time: Date,
) : Comparable<Candle> {
    override fun compareTo(other: Candle): Int = time.compareTo(other.time)
}

enum class CandleResolution {
    @SerializedName("1min")
    OneMin,

    @SerializedName("2min")
    TwoMin,

    @SerializedName("3min")
    ThreeMin,

    @SerializedName("5min")
    FiveMin,

    @SerializedName("10min")
    TenMin,

    @SerializedName("15min")
    FifteenMin,

    @SerializedName("30min")
    ThirtyMin,

    @SerializedName("hour")
    Hour,

    @SerializedName("day")
    Day,

    @SerializedName("week")
    Week,

    @SerializedName("month")
    Month
}