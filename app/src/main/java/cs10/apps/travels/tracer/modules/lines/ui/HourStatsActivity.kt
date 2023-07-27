package cs10.apps.travels.tracer.modules.lines.ui

import android.graphics.Color
import android.os.Bundle
import com.github.mikephil.charting.components.AxisBase
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entryModelOf
import cs10.apps.common.android.ui.FormActivity
import cs10.apps.travels.tracer.R
import cs10.apps.travels.tracer.Utils
import cs10.apps.travels.tracer.constants.ErrorCodes
import cs10.apps.travels.tracer.constants.Extras
import cs10.apps.travels.tracer.databinding.ActivityHourStatsBinding
import cs10.apps.travels.tracer.db.MiDB

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
            val topStopsFrom = dao.getTopStopsFrom(line)
            if (topStopsFrom.size < 2) return@doInBackground

            // vico charts
            val firstEntries = mutableListOf<FloatEntry>()
            val secondEntries = mutableListOf<FloatEntry>()

            // first chart
            dao.getHourStatsFromStop(line, topStopsFrom[0]).forEach { stat ->
                firstEntries.add(FloatEntry(stat.hour.toFloat(), stat.averageRate.toFloat()))
            }

            // second chart
            dao.getHourStatsFromStop(line, topStopsFrom[1]).forEach { stat ->
                secondEntries.add(FloatEntry(stat.hour.toFloat(), stat.averageRate.toFloat()))
            }

            // prepare x axis
            doInForeground {
                binding.firstTitle.text = binding.root.context.getString(R.string.from_stop, topStopsFrom[0])
                binding.firstChart.setModel(entryModelOf(firstEntries))

                binding.secondTitle.text = binding.root.context.getString(R.string.from_stop, topStopsFrom[1])
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