package cs10.apps.travels.tracer.modules.lines.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import cs10.apps.travels.tracer.R
import cs10.apps.travels.tracer.modules.lines.model.HourChartData

class HourChartAdapter : Adapter<HourChartVH>() {
    private val data = mutableListOf<HourChartData>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourChartVH {
        val layoutInflater = LayoutInflater.from(parent.context)
        return HourChartVH(layoutInflater.inflate(R.layout.item_hour_chart, parent, false))
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: HourChartVH, position: Int) {
        holder.render(data[position])
    }

    fun clear(){
        data.clear()
    }

    fun add(item: HourChartData){
        data.add(item)
    }
}