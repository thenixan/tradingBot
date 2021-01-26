package com.seemsnerdy.trading.api.objects.responses

import com.google.gson.annotations.SerializedName
import com.seemsnerdy.trading.api.objects.Currency
import com.seemsnerdy.trading.api.objects.InstrumentType
import java.math.BigDecimal

data class MarketStocksResponse(
    @SerializedName("trackingId") override val trackingId: String,
    @SerializedName("status") override val status: String,
    @SerializedName("payload") val payload: MarketInstrumentList
) : Response()

data class MarketInstrumentList(
    @SerializedName("total") val total: Int,
    @SerializedName("instruments") val instruments: List<MarketInstrument>
)

data class MarketInstrument(
    @SerializedName("figi") val figi: String,
    @SerializedName("ticker") val ticker: String,
    @SerializedName("isin") val isin: String,
    @SerializedName("minPriceIncrement") val minPriceIncrement: BigDecimal,
    @SerializedName("lot") val lot: Int,
    @SerializedName("minQuantity") val minQuantity: Int,
    @SerializedName("currency") val currency: Currency,
    @SerializedName("name") val name: String,
    @SerializedName("type") val type: InstrumentType
)