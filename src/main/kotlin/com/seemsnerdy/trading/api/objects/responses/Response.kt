package com.seemsnerdy.trading.api.objects.responses

abstract class Response {
    abstract val trackingId: String
    abstract val status: String
}