package cs10.apps.travels.tracer.modules.lines.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.patrykandpatrick.vico.core.axis.Axis
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.DecimalFormatAxisValueFormatter
import com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entryModelOf
import cs10.apps.travels.tracer.databinding.ItemHourChartBinding
import cs10.apps.travels.tracer.modules.lines.model.HourChartData
import java.math.RoundingMode
import kotlin.math.ceil
import kotlin.math.floor

class HourChartVH(view: View) : ViewHolder(view) {
    private val binding = ItemHourChartBinding.bind(view)

    fun render(hourChartData: HourChartData){
        binding.title.text = hourChartData.frequentTravel.toString()

        val vaf = DecimalFormatAxisValueFormatter<AxisPosition.Vertical.Start>("#.#", RoundingMode.HALF_UP)
        (binding.chart.startAxis as Axis<AxisPosition.Vertical.Start>).valueFormatter = vaf

        hourChartData.statList.also {
            val avg = it.sumOf { item -> item.averageRate } / it.size
            binding.subtitle.text = "Promedio: ${Math.round(avg)} minutos"

            val min = floor(it.minOf { item -> item.averageRate } / 10.4) * 10.0
            val max = ceil(it.maxOf { item -> item.averageRate } / 10) * 10.0
            binding.chart.chart?.axisValuesOverrider = AxisValuesOverrider.fixed(minY = min.toFloat(), maxY = max.toFloat())

            val entries = mutableListOf<FloatEntry>()
            it.forEach { stat -> entries.add(FloatEntry(stat.hour.toFloat(), stat.averageRate.toFloat())) }
            binding.chart.setModel(entryModelOf(entries))
        }


    }

}