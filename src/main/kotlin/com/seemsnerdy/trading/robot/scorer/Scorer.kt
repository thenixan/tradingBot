package com.seemsnerdy.trading.robot.scorer

interface Scorer {

    val shouldSell: Boolean
    val shouldBuy: Boolean
}