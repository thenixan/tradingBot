package com.seemsnerdy.trading.api.objects.responses

import com.google.gson.annotations.SerializedName
import com.seemsnerdy.trading.api.objects.OperationType
import com.seemsnerdy.trading.api.objects.OrderStatus
import java.math.BigDecimal

data class OrdersResponse(
    @SerializedName("trackingId") override val trackingId: String,
    @SerializedName("status") override val status: String,
    @SerializedName("payload") val payload: List<OrdersPayload>
) : Response()

data class OrdersPayload(
    @SerializedName("orderId") val orderId: String,
    @SerializedName("figi") val figi: String,
    @SerializedName("operation") val operation: OperationType,
    @SerializedName("status") val status: OrderStatus,
    @SerializedName("requestedLots") val requestedLots: Int,
    @SerializedName("executedLots") val executedLots: Int,
    @SerializedName("type") val type: OrderType,
    @SerializedName("price") val price: BigDecimal
)

enum class OrderType {
    @SerializedName("Limit")
    Limit,

    @SerializedName("Market")
    Market
}