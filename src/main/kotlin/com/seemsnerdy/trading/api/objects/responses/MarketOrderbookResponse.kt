package com.seemsnerdy.trading.api.objects.responses

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class MarketOrderbookResponse(
    @SerializedName("trackingId") override val trackingId: String,
    @SerializedName("status") override val status: String,
    @SerializedName("payload") val payload: MarketOrderbookPayload
) : Response()

data class MarketOrderbookPayload(
    @SerializedName("figi") val figi: String,
    @SerializedName("depth") val depth: Int,
    @SerializedName("bids") val bids: List<OrderResponse>,
    @SerializedName("asks") val asks: List<OrderResponse>,
    @SerializedName("tradeStatus") val tradeStatus: TradeStatus,
    @SerializedName("minPriceIncrement") val minPriceIncrement: BigDecimal,
    @SerializedName("faceValue") val faceValue: BigDecimal?,
    @SerializedName("lastPrice") val lastPrice: BigDecimal?,
    @SerializedName("closePrice") val closePrice: BigDecimal?,
    @SerializedName("limitUp") val limitUp: BigDecimal?,
    @SerializedName("limitDown") val limitDown: BigDecimal?
)

data class OrderResponse(
    @SerializedName("price") val price: BigDecimal,
    @SerializedName("quantity") val quantity: Int
)

enum class TradeStatus {
    @SerializedName("NormalTrading")
    NormalTrading,

    @SerializedName("NotAvailableForTrading")
    NotAvailableForTrading
}