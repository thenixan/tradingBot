package com.seemsnerdy.trading.chart

import com.seemsnerdy.trading.MovingAverage
import com.seemsnerdy.trading.api.objects.responses.Candle
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartUtils
import org.jfree.data.xy.DefaultHighLowDataset
import org.jfree.data.xy.XYSeries
import org.jfree.data.xy.XYSeriesCollection
import java.io.File

object ChartPainter {

    fun candleStick(out: String, name: String, candles: List<Candle>) {
        val data = candles.sortedBy { it.time }
        val chart = ChartFactory.createCandlestickChart(
            name,
            "Time",
            "Price",
            DefaultHighLowDataset(
                "a",
                data.map { it.time }.toTypedArray(),
                data.map { it.h.toDouble() }.toDoubleArray(),
                data.map { it.l.toDouble() }.toDoubleArray(),
                data.map { it.o.toDouble() }.toDoubleArray(),
                data.map { it.c.toDouble() }.toDoubleArray(),
                data.map { it.v.toDouble() }.toDoubleArray()
            ),
            false
        )
        val s = File(out).outputStream()
        ChartUtils.writeChartAsPNG(s, chart, 1280, 1024)
        s.flush()
        s.close()
    }

    fun <T> ema(out: String, name: String, vararg ema: List<MovingAverage<T>>) {
        val max = ema.map { it.size }.maxOrNull()!!
        val chart = ChartFactory.createScatterPlot(
            name,
            "Time",
            "Price",
            XYSeriesCollection()
                .also { collection ->
                    ema.forEach { e ->
                        val name = e.first().item.toString()
                        val series = XYSeries(name)
                        e.forEachIndexed { index, movingAverage ->
                            val i = max - e.size + index
                            series.add(i, movingAverage.value.toDouble())
                        }
                        collection.addSeries(series)
                    }
                }
        )
        val s = File(out).outputStream()
        ChartUtils.writeChartAsPNG(s, chart, 1280, 1024)
        s.flush()
        s.close()
    }
}