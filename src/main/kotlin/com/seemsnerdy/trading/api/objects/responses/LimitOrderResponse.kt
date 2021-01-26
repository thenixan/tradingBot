package com.seemsnerdy.trading.api.objects.responses

import com.google.gson.annotations.SerializedName
import com.seemsnerdy.trading.api.objects.MoneyAmount
import com.seemsnerdy.trading.api.objects.OperationType
import com.seemsnerdy.trading.api.objects.OrderStatus

data class LimitOrderResponse(
    @SerializedName("trackingId") override val trackingId: String,
    @SerializedName("status") override val status: String,
    @SerializedName("payload") val payload: PlacedLimitOrder
) : Response()

data class PlacedLimitOrder(
    @SerializedName("orderId") val orderId: String,
    @SerializedName("operation") val operation: OperationType,
    @SerializedName("status") val status: OrderStatus,
    @SerializedName("rejectReason") val rejectReason: String?,
    @SerializedName("message") val message: String?,
    @SerializedName("requestedLots") val requestedLots: Int,
    @SerializedName("executedLots") val executedLots: Int,
    @SerializedName("commission") val commission: MoneyAmount?,
)