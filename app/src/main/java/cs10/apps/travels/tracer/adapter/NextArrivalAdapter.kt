package cs10.apps.travels.tracer.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cs10.apps.travels.tracer.R
import cs10.apps.travels.tracer.model.joins.ScheduledParada

class NextArrivalAdapter (
    var list: List<ScheduledParada>,
    private val onClickListener: (ScheduledParada) -> Unit
) : RecyclerView.Adapter<NextArrivalViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NextArrivalViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return NextArrivalViewHolder(layoutInflater.inflate(R.layout.item_arrival, parent, false))
    }

    override fun onBindViewHolder(holder: NextArrivalViewHolder, position: Int) {
        holder.render(list[position], position < 3, onClickListener)
    }

    override fun getItemCount() = list.size
}