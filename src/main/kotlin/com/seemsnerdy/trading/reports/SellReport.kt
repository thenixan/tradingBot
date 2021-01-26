package com.seemsnerdy.trading.reports

import com.seemsnerdy.trading.api.objects.Currency
import java.math.BigDecimal
import java.math.RoundingMode

class SellReport(
    private val sold: List<SoldEntry>,
    private val notSold: List<BadPriceNotSold>,
    private val notTrading: List<NotTrading>
) : Report() {

    override val shouldSendAnyway: Boolean = sold.isNotEmpty()

    override fun text(fullText: Boolean): List<ReportBlock> {
        val result = mutableListOf<ReportBlock>()
        result.add(
            ReportBlock(title = "Sold",
                lines = sold.map {
                    "`${it.ticker}`: +${
                        it.profit.setScale(
                            2,
                            RoundingMode.UP
                        )
                    } ${it.currency.name} x${it.lots} / _${it.commission.setScale(2, RoundingMode.UP)}_"
                })
        )
        if (fullText) {
            result.add(
                ReportBlock(title = "Not sold",
                    lines = notSold.map {
                        "`${it.ticker}`: ${
                            it.price.setScale(
                                2,
                                RoundingMode.UP
                            )
                        } + ${it.commission.setScale(2, RoundingMode.UP)} ≤ ${
                            it.purchasedPrice.setScale(
                                2,
                                RoundingMode.UP
                            )
                        }"
                    })
            )
            result.add(ReportBlock(title = "Not trading", lines = notTrading.map { "`${it.ticker}`" }))
        }
        return result
    }
}

data class SoldEntry(
    val ticker: String,
    val currency: Currency,
    val soldPrice: BigDecimal,
    val lots: Int,
    val profit: BigDecimal,
    val commission: BigDecimal
)

data class BadPriceNotSold(
    val ticker: String,
    val price: BigDecimal,
    val commission: BigDecimal,
    val purchasedPrice: BigDecimal
)

data class NotTrading(
    val ticker: String
)