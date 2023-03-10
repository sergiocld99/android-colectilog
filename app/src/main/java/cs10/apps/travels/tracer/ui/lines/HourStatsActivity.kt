package cs10.apps.travels.tracer.ui.lines

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import cs10.apps.common.android.ui.FormActivity
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
            val hourStats = MiDB.getInstance(this).linesDao().getHourStatsForLine(line)
            val entries = mutableListOf<BarEntry>()
            val xLabels = mutableListOf<String>()

            hourStats.forEach { stat ->
                Log.d("HOUR_STATS", String.format("Hour %d, avgRate %.2f", stat.hour, stat.averageRate))
                val barEntry = BarEntry(stat.hour.toFloat(), stat.averageRate.toFloat())
                entries.add(barEntry)
                xLabels.add(stat.hour.toString())
            }

            //empty label for the last vertical grid line on Y-Right Axis
            xLabels.add("")

            // prepare x axis
            doInForeground {
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

                binding.barChart.axisLeft.apply {
                    applyCommonAxisOptions(this)
                    axisMinimum = 0f
                    axisMaximum = 5f
                    labelCount = 10
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
                }
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