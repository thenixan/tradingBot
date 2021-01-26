package com.seemsnerdy.trading.api

import com.google.common.util.concurrent.RateLimiter
import com.seemsnerdy.trading.api.objects.requests.LimitOrderRequest
import com.seemsnerdy.trading.api.objects.requests.SandboxRegisterRequest
import com.seemsnerdy.trading.api.objects.responses.*
import io.reactivex.rxjava3.core.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.*

interface RestApi {

    @RateLimitGroup(Rate.Orders)
    @GET("orders")
    fun orders(): Single<OrdersResponse>

    @RateLimitGroup(Rate.Portfolio)
    @GET("portfolio")
    fun portfolio(): Single<PortfolioResponse>

    @RateLimitGroup(Rate.Portfolio)
    @GET("portfolio/currencies")
    fun portfolioCurrencies(): Single<PortfolioCurrenciesResponse>

    @RateLimitGroup(Rate.Market)
    @GET("market/stocks")
    fun marketStocks(): Single<MarketStocksResponse>

    @RateLimitGroup(Rate.Market)
    @GET("market/candles")
    fun marketCandles(
        @Query("figi") figi: String,
        @Query("from") from: Date,
        @Query("to") to: Date,
        @Query("interval") interval: CandleResolution
    ): Single<MarketCandlesResponse>

    @RateLimitGroup(Rate.Market)
    @GET("market/orderbook")
    fun marketOrderbook(
        @Query("figi") figi: String,
        @Query("depth") depth: Int = 1
    ): Single<MarketOrderbookResponse>

    @RateLimitGroup(Rate.OrdersLimitOrder)
    @POST("orders/limit-order")
    fun limitOrder(
        @Query("figi") figi: String,
        @Body body: LimitOrderRequest
    ): Single<LimitOrderResponse>

    @RateLimitGroup(Rate.Market)
    @GET("market/search/by-ticker")
    fun searchByTicker(
        @Query("ticker") ticker: String
    ): Single<MarketStocksResponse>

}

interface SandboxRestApi : RestApi {

    @POST("sandbox/register")
    fun sandboxRegister(@Body request: SandboxRegisterRequest): Single<SandboxRegisterResponse>
}

enum class Rate(private val quantityPerMinute: Int) {
    Sandbox(120),
    Portfolio(120),
    Market(120),
    Orders(100),
    OrdersLimitOrder(50),
    OrdersMarketOrder(50),
    OrdersCancel(50),
    Operations(120);

    fun toRateLimit(): RateLimiter {
        val perSecond: Double = this.quantityPerMinute / 60.toDouble()
        return RateLimiter.create(perSecond)
    }
}

annotation class RateLimitGroup(val rate: Rate)