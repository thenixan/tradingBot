package com.seemsnerdy.trading.robot

sealed class Trend<T>(val value: T) {

    class Rising<T>(value: T) : Trend<T>(value)

    class Falling<T>(value: T) : Trend<T>(value)
}