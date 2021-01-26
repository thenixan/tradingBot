package com.seemsnerdy.trading.api

import com.google.gson.annotations.SerializedName
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Invocation
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.time.Duration
import java.util.*


sealed class Api<T : RestApi> {

    protected abstract val baseUrl: String

    abstract val calls: T

    protected abstract val token: String

    private val client: OkHttpClient by lazy {
        val builder = OkHttpClient().newBuilder()
        builder.readTimeout(Duration.ofSeconds(10))
        builder.connectTimeout(Duration.ofSeconds(5))
        builder.addInterceptor(RateLimitInterceptor())
        builder.addInterceptor {
            val request = it.request()
                .newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
            it.proceed(request)
        }
//        builder.addInterceptor(HttpLoggingInterceptor().also { it.level = HttpLoggingInterceptor.Level.HEADERS })
        builder.build()
    }

    protected val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .addConverterFactory(ApiConverter())
            .build()
    }

    class Production(override val token: String) : Api<RestApi>() {
        override val baseUrl: String
            get() = "https://api-invest.tinkoff.ru/openapi/"

        override val calls: RestApi = retrofit.create(RestApi::class.java)

    }

    class Sandbox(override val token: String) : Api<SandboxRestApi>() {

        override val baseUrl: String
            get() = "https://api-invest.tinkoff.ru/openapi/sandbox/"

        override val calls: SandboxRestApi = retrofit.create(SandboxRestApi::class.java)

    }

}

private class ApiConverter : Converter.Factory() {

    override fun stringConverter(
        type: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): Converter<*, String>? {
        if (type is Class<*> && type.isEnum) {
            return Converter { value: Any ->
                (value as Enum<*>)::class.java.getField(value.name).getAnnotation(SerializedName::class.java).value
            }
        }
        if (type === Date::class.java) {
            return Converter { value: Date -> SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").format(value) }
        }
        return null
    }
}

private class RateLimitInterceptor : Interceptor {

    private val limiters = Rate.values()
        .map { rate -> (rate to rate.toRateLimit()) }
        .toMap()
        .also { println(it) }

    override fun intercept(chain: Interceptor.Chain): Response {
        val rateGroup = chain.request()
            .tag(Invocation::class.java)
            ?.method()
            ?.getAnnotation(RateLimitGroup::class.java)
        rateGroup?.let { limiters[it.rate] }?.acquire()

        return chain.proceed(chain.request())
    }

}