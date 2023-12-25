package cs10.apps.travels.tracer.modules.lines.ui

import android.graphics.Color
import android.os.Bundle
import com.github.mikephil.charting.components.AxisBase
import com.patrykandpatrick.vico.core.axis.Axis
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.axis.formatter.DecimalFormatAxisValueFormatter
import com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entryModelOf
import cs10.apps.common.android.ui.FormActivity
import cs10.apps.travels.tracer.Utils
import cs10.apps.travels.tracer.constants.ErrorCodes
import cs10.apps.travels.tracer.constants.Extras
import cs10.apps.travels.tracer.databinding.ActivityHourStatsBinding
import cs10.apps.travels.tracer.db.MiDB
import java.math.RoundingMode

class HourStatsActivity : FormActivity() {
    private lateinit var binding: ActivityHourStatsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHourStatsBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        buildStats()
        Utils.loadBusBanner(binding.appbarImage)
    }

    private fun buildStats(){
        val line = intent.getIntExtra(Extras.LINE_NUMBER.name, ErrorCodes.INVALID_LINE.code)
        if (line == ErrorCodes.INVALID_LINE.code) return

        binding.toolbarLayout.title = String.format("Linea %d", line)

        doInBackground {
            val dao = MiDB.getInstance(this).linesDao()
            val topStopsFrom = dao.getFrequentTravelsFromLine(line)
            if (topStopsFrom.size < 2) return@doInBackground

            // vico charts
            val firstEntries = mutableListOf<FloatEntry>()
            val secondEntries = mutableListOf<FloatEntry>()
            var min = 9999.0
            var max = 0.0

            // first chart
            dao.getHourStatsForTravel(line, topStopsFrom[0].nombrePdaInicio, topStopsFrom[0].nombrePdaFin).forEach { stat ->
                firstEntries.add(FloatEntry(stat.hour.toFloat(), stat.averageRate.toFloat()))
                if (min > stat.averageRate) min = stat.averageRate
                if (max < stat.averageRate) max = stat.averageRate
            }

            // second chart
            dao.getHourStatsForTravel(line, topStopsFrom[1].nombrePdaInicio, topStopsFrom[1].nombrePdaFin).forEach { stat ->
                secondEntries.add(FloatEntry(stat.hour.toFloat(), stat.averageRate.toFloat()))
                if (min > stat.averageRate) min = stat.averageRate
                if (max < stat.averageRate) max = stat.averageRate
            }

            // prepare x axis
            doInForeground {
                val vaf = DecimalFormatAxisValueFormatter<AxisPosition.Vertical.Start>("#.#", RoundingMode.HALF_UP)

                val haf = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
                    String.format("%.0f hs", value)
                }

                // round
                min = (min / 10).toInt() * 10.0
                max = ((max / 10).toInt() + 1) * 10.0

                binding.firstTitle.text = topStopsFrom[0].toString()
                //binding.firstChart.chart?.axisValuesOverrider = AxisValuesOverrider.fixed(0f, 23f, 0f, 100f)
                (binding.firstChart.startAxis as Axis<AxisPosition.Vertical.Start>).valueFormatter = vaf
                binding.firstChart.chart?.axisValuesOverrider = AxisValuesOverrider.fixed(minY = min.toFloat(), maxY = max.toFloat())
                binding.firstChart.setModel(entryModelOf(firstEntries))

                binding.secondTitle.text = topStopsFrom[1].toString()
                (binding.secondChart.startAxis as Axis<AxisPosition.Vertical.Start>).valueFormatter = vaf
                binding.secondChart.chart?.axisValuesOverrider = AxisValuesOverrider.fixed(minY = min.toFloat(), maxY = max.toFloat())
                binding.secondChart.setModel(entryModelOf(secondEntries))

                /*
                binding.barChart.xAxis.apply {
                    enableGridDashedLine(10f, 10f, 0f)
                    applyCommonAxisOptions(this)
                    axisMinimum = 0f
                    axisMaximum = 23f
                    labelCount = 12
                    position = XAxis.XAxisPosition.BOTTOM
                    xOffset = 0f
                    yOffset = 0f
                }

                min = (min - 10).div(10).roundToInt().times(10.0)
                max = (max + 10).div(10).roundToInt().times(10.0)

                binding.barChart.axisLeft.apply {
                    applyCommonAxisOptions(this)
                    axisMinimum = min.toFloat()
                    axisMaximum = max.toFloat()
                    labelCount = (max - min).div(5).roundToInt() + 0
                    setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
                }

                binding.barChart.axisRight.apply {
                    axisMinimum = 0f
                    setDrawAxisLine(true)
                    labelCount = 0

                }

                // dataset bars
                val barDataSet = BarDataSet(entries, "hs")
                barDataSet.apply {
                    color = Color.BLUE
                    formSize = 15f
                    setDrawValues(false)
                    valueTextSize = 12f
                }

                // add to chart
                val data = BarData(barDataSet)

                binding.barChart.apply {
                    setData(data)
                    setScaleEnabled(false)
                    legend.isEnabled = false
                    setDrawBarShadow(false)
                    description.isEnabled = false
                    setPinchZoom(false)
                    setDrawGridBackground(true)
                    invalidate()
                } */
            }
        }
    }

    private fun applyCommonAxisOptions(axisBase: AxisBase){
        axisBase.apply {
            textColor = Color.WHITE
            textSize = 14f
            setDrawAxisLine(true)
            axisLineColor = Color.WHITE
            setDrawGridLines(true)
            granularity = 0.1f
            isGranularityEnabled = true
        }
    }
}