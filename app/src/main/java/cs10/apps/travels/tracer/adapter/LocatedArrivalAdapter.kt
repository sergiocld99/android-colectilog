package cs10.apps.travels.tracer.adapter

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cs10.apps.travels.tracer.R
import cs10.apps.travels.tracer.model.Viaje
import cs10.apps.travels.tracer.model.roca.ArriboTren

class LocatedArrivalAdapter(
    var list: MutableList<Viaje>,
    private val onClickListener: (ArriboTren) -> Unit
) : RecyclerView.Adapter<LocatedArrivalViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocatedArrivalViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return LocatedArrivalViewHolder(layoutInflater.inflate(R.layout.item_arrival, parent, false))
    }

    override fun onBindViewHolder(holder: LocatedArrivalViewHolder, position: Int) {
        holder.render(list[position], position == 0, onClickListener) { viaje ->
            if (itemCount > 0 && list[0] == viaje) {
                list.removeAt(0)
                notifyItemRemoved(0)
                Handler(Looper.getMainLooper()).postDelayed({ notifyDataSetChanged() }, 1500)
            }
        }
    }

    override fun getItemCount() = list.size
}