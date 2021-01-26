package com.seemsnerdy.trading.robot

import com.google.gson.annotations.SerializedName
import com.seemsnerdy.trading.api.objects.Currency
import java.math.BigDecimal
import java.util.*

data class Balance(
    @SerializedName("time") val time: Date = Date(),
    @SerializedName("balances") val balances: List<BalanceEntry>
)

data class BalanceEntry(
    @SerializedName("ccy") val currency: Currency,
    @SerializedName("amount") val amount: BigDecimal
)