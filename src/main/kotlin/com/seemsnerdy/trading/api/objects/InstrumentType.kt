package com.seemsnerdy.trading.api.objects

import com.google.gson.annotations.SerializedName

enum class InstrumentType {
    @SerializedName("Stock")
    Stock,

    @SerializedName("Currency")
    Currency,

    @SerializedName("Bond")
    Bond,

    @SerializedName("Etf")
    Etf
}