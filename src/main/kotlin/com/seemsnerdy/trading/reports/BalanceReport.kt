package com.seemsnerdy.trading.reports

import com.google.gson.Gson
import com.seemsnerdy.trading.api.objects.Currency
import com.seemsnerdy.trading.robot.Balance
import com.seemsnerdy.trading.robot.BalanceEntry
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.math.BigDecimal
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class BalanceReport(
    private val money: TreeMap<Currency, BigDecimal>,
    private val portfolio: TreeMap<Currency, BigDecimal>
) : Report() {

    override val shouldSendAnyway: Boolean = false

    override fun text(overrideSending: Boolean): List<ReportBlock> {
        val summary = (money.asSequence() + portfolio.asSequence())
            .groupingBy { it.key }
            .fold(BigDecimal.ZERO, { acc, item -> acc + item.value })


        val balanceHistory = readBalance(summary)

        return listOf(
            ReportBlock(title = "\uD83D\uDCB0 Money",
                lines = money.map { "`${it.key.name}`: ${it.value.setScale(2)}" }),
            ReportBlock(title = "\uD83D\uDCBC Portfolio",
                lines = portfolio.map { "`${it.key.name}`: ${it.value.setScale(2)}" }),
            ReportBlock(
                title = "\uD83D\uDCB1 Total",
                lines = listOf(
                    "_Compared to ${
                        SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                            .format(balanceHistory.time)
                    }_"
                ) + summary.map { (t, u) ->
                    val change = u - (balanceHistory.balances.find { it.currency == t }?.amount ?: BigDecimal.ZERO)
                    when {
                        change > BigDecimal.ZERO -> {
                            "`${t.name}`: ${u.setScale(2)} \uD83D\uDD3A${change.abs().setScale(2)}"
                        }
                        change < BigDecimal.ZERO -> {
                            "`${t.name}`: ${u.setScale(2)} \uD83D\uDD3B${change.abs().setScale(2)}"
                        }
                        else -> {
                            "`${t.name}`: ${u.setScale(2)}"
                        }
                    }
                }
            )
        )
    }

    private fun readBalance(summary: Map<Currency, BigDecimal>): Balance {
        val gson = Gson()

        val f = File("balances.json")
        val balanceHistory: Balance? =
            f.takeIf { it.exists() }?.let { gson.fromJson(FileReader(it), Balance::class.java) }

        return if (balanceHistory == null) {
            val b = Balance(time = Date(), balances = summary.map { BalanceEntry(it.key, it.value) })
            val w = FileWriter(f)
            gson.toJson(b, w)
            w.flush()
            w.close()
            b
        } else {
            balanceHistory
        }
    }
}