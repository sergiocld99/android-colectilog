package cs10.apps.travels.tracer.modules.lines.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cs10.apps.travels.tracer.R
import cs10.apps.travels.tracer.model.joins.RatedBusLine
import cs10.apps.travels.tracer.model.lines.CustomBusLine

class LineManagerAdapter(
    var list: List<RatedBusLine>,
    private val onLineClick: (CustomBusLine) -> Unit
) : RecyclerView.Adapter<LineViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LineViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return LineViewHolder(layoutInflater.inflate(R.layout.item_line, parent, false))
    }

    override fun onBindViewHolder(holder: LineViewHolder, position: Int) {
        holder.render(list[position], onLineClick)
    }

    override fun getItemCount() = list.size

}
