package com.seemsnerdy.trading.api.objects.requests

import com.google.gson.annotations.SerializedName
import com.seemsnerdy.trading.api.objects.OperationType
import java.math.BigDecimal

data class LimitOrderRequest(
    @SerializedName("lots") val lots: Int,
    @SerializedName("operation") val operation: OperationType,
    @SerializedName("price") val price: BigDecimal
)