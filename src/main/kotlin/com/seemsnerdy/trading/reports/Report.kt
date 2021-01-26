package com.seemsnerdy.trading.reports

abstract class Report() {

    abstract val shouldSendAnyway: Boolean

    abstract fun text(fullText: Boolean): List<ReportBlock>
}

data class ReportBlock(val title: String, val lines: List<String>) {
    override fun toString(): String {
        val result = StringBuilder()
        result.appendLine("*$title*:")
        lines.takeIf { it.isNotEmpty() }?.forEach { result.appendLine(it) } ?: result.appendLine("–")
        return result.toString()
    }
}