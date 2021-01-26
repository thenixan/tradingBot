package com.seemsnerdy.trading.api.objects.responses

import com.google.gson.annotations.SerializedName

data class ErrorResponse(
    @SerializedName("trackingId") override val trackingId: String,
    @SerializedName("status") override val status: String,
    @SerializedName("payload") val payload: ErrorPayload
) : Response()

data class ErrorPayload(
    @SerializedName("message") val message: String,
    @SerializedName("code") val code: String
)