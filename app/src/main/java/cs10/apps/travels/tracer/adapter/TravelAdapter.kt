package cs10.apps.travels.tracer.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cs10.apps.travels.tracer.R
import cs10.apps.travels.tracer.model.Viaje

class TravelAdapter(
    private val onClickListener: (Viaje) -> Unit,
    private val onLongClickListener: (Viaje, Int) -> Unit
) : RecyclerView.Adapter<TravelViewHolder>() {

    var list = mutableListOf<Viaje>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TravelViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return TravelViewHolder(layoutInflater.inflate(R.layout.item_travel, parent, false))
    }

    override fun onBindViewHolder(holder: TravelViewHolder, position: Int) {
        holder.render(list[position], position, onClickListener, onLongClickListener)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    // deletes item from adapter list and updates recycler
    fun remove(pos: Int) {
        list.removeAt(pos)
        notifyItemRemoved(pos)
    }

}