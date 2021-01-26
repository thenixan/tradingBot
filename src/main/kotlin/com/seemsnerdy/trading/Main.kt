package com.seemsnerdy.trading

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.entities.ParseMode
import com.seemsnerdy.trading.api.Api
import com.seemsnerdy.trading.api.objects.Currency
import com.seemsnerdy.trading.api.objects.OperationType
import com.seemsnerdy.trading.api.objects.requests.LimitOrderRequest
import com.seemsnerdy.trading.api.objects.responses.MarketInstrument
import com.seemsnerdy.trading.api.objects.responses.TradeStatus
import com.seemsnerdy.trading.reports.*
import com.seemsnerdy.trading.robot.Robot
import io.github.cdimascio.dotenv.dotenv
import retrofit2.HttpException
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import kotlin.random.Random

val COMISSION = BigDecimal(0.0005)


data class Tried(val ticker: String, val belowProfit: BigDecimal, val ccy: Currency)
data class Sold(val ticker: String, val profit: BigDecimal, val ccy: Currency)
data class PortfolioReport(val tried: List<Tried> = emptyList(), val sold: List<Sold> = emptyList()) {
    fun recordSell(sell: Sold): PortfolioReport =
        copy(sold = mutableListOf<Sold>().also { it.addAll(sold) }.also { it.add(sell) })
}

var stocksLoop: Iterator<MarketInstrument>? = null

fun main() {

    val dotenv = dotenv()

    val chatId = dotenv.get("ADMIN_CHAT_ID").toLong()

    val robot = Robot(Api.Production(dotenv.get("TINKOFF_TOKEN")).calls)

    var telegramBot: Bot? = null

    val reporter = { report: Report, fullText: Boolean ->
        val text = report.text(fullText).joinToString(separator = "\n\n")
        telegramBot?.sendMessage(chatId = chatId, text = text, parseMode = ParseMode.MARKDOWN)
    }

    val taskQueue = RecurringTasks(reporter, robot)

    val stocksList = robot.getStocks()
    stocksLoop = stocksList.randomLooper()

    taskQueue.run()


    telegramBot = bot {
        token = dotenv.get("TELEGRAM_TOKEN")
        dispatch {
            filteredCommand("balance", chatId) {
                bot.sendMessage(chatId, text = "Preparing...")
                taskQueue.enqueue(Task.BalanceCheck(withReport = true))
            }
            filteredCommand("portfolio", chatId) {
                bot.sendMessage(chatId, text = "Preparing...")
                taskQueue.enqueue(Task.PortfolioCheck(withReport = true))
            }
            filteredCommand("purchase", chatId) {
                bot.sendMessage(chatId, text = "Preparing...")
                taskQueue.enqueue(Task.SearchNew(withReport = true))
            }
            filteredCommand("help", chatId) {
                bot.sendMessage(
                    chatId, text = """
                    /balance - to get balances
                    /portfolio - to get portfolio report
                    /purchase - to get purchase report
                """.trimIndent()
                )
            }
        }
    }


    telegramBot.startPolling()

}

fun runBalanceCheck(robot: Robot, shouldStop: ShouldStop): Report {
    val money = robot.balances().filter { it.value != BigDecimal.ZERO }
    val portfolio = robot.stocksBalances().filter { it.value != BigDecimal.ZERO }

    return BalanceReport(money = TreeMap(money), portfolio = TreeMap(portfolio))
}

fun runSellScoring(robot: Robot, shouldStop: ShouldStop): Report {
    val badPrice = mutableListOf<BadPriceNotSold>()
    val notTrading = mutableListOf<NotTrading>()
    val sold =
        robot.runPortfolioScoring()
            .filter { it.score.shouldSell }
            .map {
                val response = robot.api
                    .marketOrderbook(figi = it.portfolioPosition.figi)
                    .retry()
                    .blockingGet()
                it to response
            }
            .filter { pair ->
                if (pair.second.payload.tradeStatus == TradeStatus.NotAvailableForTrading) {
                    notTrading.add(NotTrading(pair.first.portfolioPosition.ticker))
                    false
                } else {
                    true
                }
            }
            .filter { pair ->
                val bidPrice = pair.second.payload.bids.firstOrNull() ?: return@filter false
                val comission = bidPrice.price.multiply(COMISSION)
                    .setScale(2, RoundingMode.UP) + (pair.first.portfolioPosition.averagePositionPrice?.value
                    ?: BigDecimal.ZERO).divide(
                    BigDecimal(pair.first.portfolioPosition.lots)
                ).multiply(COMISSION).setScale(2, RoundingMode.UP)
                    .multiply(
                        BigDecimal(pair.first.portfolioPosition.lots)
                    )

                val purchasedPrice = pair.first.portfolioPosition.averagePositionPrice ?: return@filter false

                val result =
                    purchasedPrice.value < (bidPrice.price - comission) && pair.first.portfolioPosition.lots <= bidPrice.quantity
                if (!result) {
                    badPrice.add(
                        BadPriceNotSold(
                            ticker = pair.first.portfolioPosition.ticker,
                            price = bidPrice.price,
                            commission = comission,
                            purchasedPrice = purchasedPrice.value
                        )
                    )
                }
                result
            }
            .filter { pair ->
                robot.api
                    .limitOrder(
                        figi = pair.first.portfolioPosition.figi,
                        body = LimitOrderRequest(
                            lots = pair.first.portfolioPosition.lots,
                            price = pair.second.payload.bids.first().price
                                .setScale(2, RoundingMode.HALF_UP),
                            operation = OperationType.Sell
                        )
                    )
                    .onErrorComplete()
                    .blockingGet() != null
            }
            .map {
                println("${it.first.portfolioPosition.ticker}: sold")
                SoldEntry(
                    ticker = it.first.portfolioPosition.ticker,
                    currency = it.first.portfolioPosition.averagePositionPrice?.currency ?: Currency.USD,
                    soldPrice = it.second.payload.bids.first().price,
                    lots = it.first.portfolioPosition.lots,
                    profit = BigDecimal(
                        it.first.portfolioPosition.lots
                    ) * (it.second.payload.bids.first().price - (it.first.portfolioPosition.averagePositionPrice?.value
                        ?: BigDecimal.ZERO)),
                    commission = it.second.payload.bids.first().price.multiply(COMISSION)
                        .setScale(2, RoundingMode.UP) + (it.first.portfolioPosition.averagePositionPrice?.value
                        ?: BigDecimal.ZERO).divide(
                        BigDecimal(it.first.portfolioPosition.lots)
                    ).multiply(COMISSION).setScale(2, RoundingMode.UP)
                        .multiply(
                            BigDecimal(it.first.portfolioPosition.lots)
                        )
    )
}
return SellReport(sold = sold, notSold = badPrice, notTrading = notTrading)
}

fun runPurchaseScoring(robot: Robot, shouldStop: ShouldStop): Report {
    var balances = robot.balances()

    val purchased = mutableListOf<PurchaseEntry>()
    val notEnoughBalance = mutableListOf<NotEnoughBalance>()
    val unavailable = mutableListOf<NotAvailableForTrading>()
    var examinedNumber = 0

    while (!shouldStop()) {
        stocksLoop?.next()?.let { stock ->

            val score = robot.getScoring(stock.figi, stock.ticker).blockingGet()
            val shouldBuy = score != null && score.isWorking && score.shouldBuy
            if (shouldBuy) {
                val available = balances.getOrDefault(stock.currency, BigDecimal.ZERO)
                val marketOrderbook = robot.api
                    .marketOrderbook(stock.figi)
                    .retry { i, t ->
                        println("Retrying ${stock.ticker} orderbook the $i time: $t")
                        t !is HttpException
                    }
                    .blockingGet()
                if (marketOrderbook.payload.tradeStatus == TradeStatus.NotAvailableForTrading) {
                    unavailable.add(NotAvailableForTrading(stock.ticker))
                } else {
                    marketOrderbook.payload.asks.firstOrNull()?.let { price ->
                        if (available > price.price * BigDecimal(stock.lot)) {
                            marketOrderbook.payload
                                .asks
                                .firstOrNull()?.let { ask ->
                                    if (ask.quantity >= 1 && ask.price * BigDecimal(stock.lot) < available) {
                                        val result = robot.api
                                            .limitOrder(
                                                figi = stock.figi,
                                                body = LimitOrderRequest(
                                                    lots = stock.lot,
                                                    price = ask.price,
                                                    operation = OperationType.Buy
                                                )
                                            )
                                            .onErrorComplete()
                                            .blockingGet()

                                        result?.let {
                                            println("${stock.ticker}: purchased")
                                            purchased.add(
                                                PurchaseEntry(
                                                    ticker = stock.ticker,
                                                    currency = stock.currency,
                                                    amount = ask.price * BigDecimal(stock.lot)
                                                )
                                            )
                                        }
                                        balances = robot.balances()
                                    }
                                }
                        } else {
                            notEnoughBalance.add(
                                NotEnoughBalance(
                                    ticker = stock.ticker,
                                    currency = stock.currency,
                                    price = price.price,
                                    available = available
                                )
                            )
                        }
                    }
                }
            }
            examinedNumber += 1
        }
    }
    return PurchaseReport(examinedNumber, purchased, notEnoughBalance, unavailable)
}

fun <T> List<T>.randomLooper(): Iterator<T> = object : Iterator<T> {

    val shuffled = shuffled()

    var currentPosition = Random.nextInt(0, size)

    override fun hasNext(): Boolean = true

    override fun next(): T {
        if (currentPosition == size) {
            currentPosition = 0
        }
        return shuffled[currentPosition].also { currentPosition += 1 }
    }

}