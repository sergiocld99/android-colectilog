package cs10.apps.travels.tracer.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cs10.apps.travels.tracer.R
import cs10.apps.travels.tracer.model.roca.RamalSchedule

class NearStopAdapter(
    var list: List<RamalSchedule>,
    private val onClickListener: (RamalSchedule) -> Unit
) : RecyclerView.Adapter<NearStopViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NearStopViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return NearStopViewHolder(layoutInflater.inflate(R.layout.item_near_stop, parent, false))
    }

    override fun onBindViewHolder(holder: NearStopViewHolder, position: Int) {
        holder.render(list[position], onClickListener)
    }

    override fun getItemCount() = list.size
}