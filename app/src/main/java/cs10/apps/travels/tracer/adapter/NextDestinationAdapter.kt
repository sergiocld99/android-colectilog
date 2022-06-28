package cs10.apps.travels.tracer.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cs10.apps.travels.tracer.R
import cs10.apps.travels.tracer.model.ScheduledParada

class NextDestinationAdapter(
    var list: List<ScheduledParada>,
    val onClickListener: (ScheduledParada) -> Unit
) : RecyclerView.Adapter<NextDestinationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NextDestinationViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return NextDestinationViewHolder(layoutInflater.inflate(R.layout.item_stop, parent, false))
    }

    override fun onBindViewHolder(holder: NextDestinationViewHolder, position: Int) {
        holder.render(list[position], position < 3, onClickListener)
    }

    override fun getItemCount(): Int {
        return list.size
    }
}