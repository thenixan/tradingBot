package com.seemsnerdy.trading.api.objects.responses

import com.google.gson.annotations.SerializedName
import com.seemsnerdy.trading.api.objects.InstrumentType
import com.seemsnerdy.trading.api.objects.MoneyAmount
import java.math.BigDecimal

data class PortfolioResponse(
    @SerializedName("trackingId") override val trackingId: String,
    @SerializedName("status") override val status: String,
    @SerializedName("payload") val payload: Portfolio
) : Response()

data class Portfolio(
    @SerializedName("positions") val positions: List<PortfolioPosition>
)

data class PortfolioPosition(
    @SerializedName("figi") val figi: String,
    @SerializedName("ticker") val ticker: String,
    @SerializedName("isin") val isin: String,
    @SerializedName("instrumentType") val instrumentType: InstrumentType,
    @SerializedName("balance") val balance: BigDecimal,
    @SerializedName("blocked") val blocked: BigDecimal,
    @SerializedName("expectedYield") val expectedYield: MoneyAmount,
    @SerializedName("lots") val lots: Int,
    @SerializedName("averagePositionPrice") val averagePositionPrice: MoneyAmount?,
    @SerializedName("averagePositionPriceNoNkd") val averagePositionPriceNoNkd: MoneyAmount,
    @SerializedName("name") val name: String,
)
