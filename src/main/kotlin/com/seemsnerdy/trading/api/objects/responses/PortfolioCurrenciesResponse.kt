package com.seemsnerdy.trading.api.objects.responses

import com.google.gson.annotations.SerializedName
import com.seemsnerdy.trading.api.objects.Currency
import java.math.BigDecimal

data class PortfolioCurrenciesResponse(
    @SerializedName("trackingId") override val trackingId: String,
    @SerializedName("status") override val status: String,
    @SerializedName("payload") val payload: Currencies,
) : Response()

data class Currencies(
    @SerializedName("currencies") val currencies: List<CurrencyPosition>
)

data class CurrencyPosition(
    @SerializedName("currency") val currency: Currency,
    @SerializedName("balance") val balance: BigDecimal,
    @SerializedName("blocked") val blocked: BigDecimal,
)