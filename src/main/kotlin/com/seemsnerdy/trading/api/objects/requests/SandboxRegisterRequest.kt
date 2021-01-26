package com.seemsnerdy.trading.api.objects.requests

import com.google.gson.annotations.SerializedName
import com.seemsnerdy.trading.api.objects.BrokerAccountType

data class SandboxRegisterRequest(
    @SerializedName("brokerAccountType") val brokerAccountType: BrokerAccountType
)