package com.seemsnerdy.trading.reports

import com.seemsnerdy.trading.api.objects.Currency
import java.math.BigDecimal
import java.math.RoundingMode

class PurchaseReport(
    private val examinedNumber: Int,
    private val purchased: List<PurchaseEntry>,
    private val notEnoughBalance: List<NotEnoughBalance>,
    private val notAvailableForTrading: List<NotAvailableForTrading>
) : Report() {

    override val shouldSendAnyway: Boolean = purchased.isNotEmpty()

    override fun text(fullText: Boolean): List<ReportBlock> {
        val blocks = mutableListOf<ReportBlock>()
        blocks.add(ReportBlock(
            title = "Purchased",
            lines = purchased.map {
                "`${it.ticker}`: ${
                    it.amount.setScale(
                        2,
                        RoundingMode.UP
                    )
                } ${it.currency.name}"
            }
        ))
        if (fullText) {
            blocks.add(
                ReportBlock(
                    title = "Not enough money",
                    lines = notEnoughBalance.map {
                        "`${it.ticker}`: ${
                            it.price.setScale(
                                2,
                                RoundingMode.UP
                            )
                        } ${it.currency.name} ≥ ${it.available.setScale(2, RoundingMode.UP)}"
                    }
                ))
            blocks.add(
                ReportBlock(title = "Not available for trading",
                    lines = notAvailableForTrading.map { "`${it.ticker}`" })
            )
            blocks.add(ReportBlock(title = "Examined", lines = listOf("$examinedNumber")))
        }
        return blocks
    }


}

data class PurchaseEntry(val ticker: String, val currency: Currency, val amount: BigDecimal)
data class NotEnoughBalance(
    val ticker: String,
    val currency: Currency,
    val price: BigDecimal,
    val available: BigDecimal
)

data class NotAvailableForTrading(val ticker: String)