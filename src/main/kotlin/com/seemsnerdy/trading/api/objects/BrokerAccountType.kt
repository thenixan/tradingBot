package com.seemsnerdy.trading.api.objects

import com.google.gson.annotations.SerializedName

enum class BrokerAccountType {
    @SerializedName("Tinkoff")
    Tinkoff,

    @SerializedName("TinkoffIis")
    TinkoffIis
}