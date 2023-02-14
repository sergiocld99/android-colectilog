package cs10.apps.travels.tracer.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cs10.apps.travels.tracer.R
import cs10.apps.travels.tracer.model.Zone

class ZoneManagerAdapter(
    var list: MutableList<Zone>,
    private val onClick: (Zone) -> Unit,
    private val onLongClick: (Zone, Int) -> Unit
) : RecyclerView.Adapter<ZoneViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ZoneViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ZoneViewHolder(layoutInflater.inflate(R.layout.item_zone, parent, false))
    }

    override fun onBindViewHolder(holder: ZoneViewHolder, position: Int) {
        holder.render(list[position], onClick, onLongClick)
    }

    override fun getItemCount() = list.size

    // deletes item from adapter list and updates recycler
    fun remove(pos: Int) {
        list.removeAt(pos)
        notifyItemRemoved(pos)
    }
}
