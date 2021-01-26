package com.seemsnerdy.trading.api.objects

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class MoneyAmount(
    @SerializedName("currency") val currency: Currency,
    @SerializedName("value") val value: BigDecimal
)

enum class Currency {
    @SerializedName("RUB")
    RUB,

    @SerializedName("USD")
    USD,

    @SerializedName("EUR")
    EUR,

    @SerializedName("GBP")
    GBP,

    @SerializedName("HKD")
    HKD,

    @SerializedName("CHF")
    CHF,

    @SerializedName("JPY")
    JPY,

    @SerializedName("CNY")
    CNY,

    @SerializedName("TRY")
    TRY
}