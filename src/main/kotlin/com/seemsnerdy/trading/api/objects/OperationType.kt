package com.seemsnerdy.trading.api.objects

import com.google.gson.annotations.SerializedName

enum class OperationType {
    @SerializedName("Buy")
    Buy,

    @SerializedName("Sell")
    Sell
}