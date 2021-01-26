package com.seemsnerdy.trading

import com.seemsnerdy.trading.reports.Report
import com.seemsnerdy.trading.robot.Robot
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.time.Duration
import java.time.Instant
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayDeque
import kotlin.concurrent.thread

class RecurringTasks(val reporter: (Report, Boolean) -> Any?, val robot: Robot) {

    private val stack = ArrayDeque<Task>()

    fun enqueue(task: Task) {
        stack.add(task)
    }

    val next: Task
        get() =
            if (stack.isEmpty()) {
                Task.SearchNew()
            } else {
                stack.removeFirst()
            }

    fun shouldTerminatePurchasing(): Boolean = stack.isNotEmpty()

    fun run() = thread(start = true) {
        Observable.interval(2, TimeUnit.MINUTES)
            .subscribeOn(Schedulers.newThread())
            .observeOn(Schedulers.newThread())
            .subscribe { enqueue(Task.PortfolioCheck()) }
        enqueue(Task.PortfolioCheck())
        while (true) {
            val n = next
            println("${Date()} - $n")
            val terminator = ::shouldTerminatePurchasing.takeIf { n is Task.SearchNew } ?: { true }
            val before = Instant.now()
            val report = n.runner(robot, terminator)
            val after = Instant.now()
            println(
                DateTimeFormatter.ISO_LOCAL_TIME.format(
                    LocalTime.ofNanoOfDay(
                        Duration.between(before, after).toNanos()
                    )
                )
            )
            if (report.shouldSendAnyway || n.withReport) {
                reporter(report, n.withReport)
            }
        }
    }

}

typealias ShouldStop = () -> Boolean

sealed class Task(val withReport: Boolean) {

    abstract val runner: (Robot, ShouldStop) -> Report

    class PortfolioCheck(withReport: Boolean = false) : Task(withReport) {
        override val runner: (Robot, ShouldStop) -> Report = ::runSellScoring
        override fun toString(): String = "Portfolio check, withReport=$withReport"
    }

    class BalanceCheck(withReport: Boolean = false) : Task(withReport) {
        override val runner: (Robot, ShouldStop) -> Report = ::runBalanceCheck
        override fun toString(): String = "Balance check, withReport=$withReport"
    }

    class SearchNew(withReport: Boolean = false) : Task(withReport) {
        override val runner: (Robot, ShouldStop) -> Report = ::runPurchaseScoring
        override fun toString(): String = "Search for new, withReport=$withReport"
    }
}