package cs10.apps.travels.tracer.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cs10.apps.travels.tracer.R
import cs10.apps.travels.tracer.model.joins.BusInfo

class BusRamalsAdapter(
    var list: List<BusInfo>,
    private val onItemClick: (BusInfo) -> Unit
) : RecyclerView.Adapter<BusRamalViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BusRamalViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return BusRamalViewHolder(layoutInflater.inflate(R.layout.item_line, parent, false))
    }

    override fun onBindViewHolder(holder: BusRamalViewHolder, position: Int) {
        holder.render(list[position], onItemClick)
    }

    override fun getItemCount() = list.size

}
