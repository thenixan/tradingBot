package com.seemsnerdy.trading.api.objects.responses

import com.google.gson.annotations.SerializedName
import com.seemsnerdy.trading.api.objects.BrokerAccountType

data class SandboxRegisterResponse(
    @SerializedName("trackingId") override val trackingId: String,
    @SerializedName("status") override val status: String,
    @SerializedName("payload") val payload: SandboxAccount,
) : Response()

data class SandboxAccount(
    @SerializedName("brokerAccountType") val brokerAccountType: BrokerAccountType,
    @SerializedName("brokerAccountId") val brokerAccountId: String
)