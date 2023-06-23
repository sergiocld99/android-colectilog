package cs10.apps.travels.tracer.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import cs10.apps.travels.tracer.R
import cs10.apps.travels.tracer.enums.TransportType
import cs10.apps.travels.tracer.model.Viaje
import cs10.apps.travels.tracer.model.joins.ColoredTravel

class TravelAdapter(
    private val onClickListener: (Viaje) -> Unit,
    private val onLongClickListener: (Viaje, Int) -> Unit
) : RecyclerView.Adapter<TravelViewHolder>(), Filterable {

    private val originalList = mutableListOf<ColoredTravel>()
    private var filteredList = listOf<ColoredTravel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TravelViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return TravelViewHolder(layoutInflater.inflate(R.layout.item_travel, parent, false))
    }

    override fun onBindViewHolder(holder: TravelViewHolder, position: Int) {
        holder.render(filteredList[position], onClickListener, onLongClickListener)
    }

    override fun getItemCount(): Int = filteredList.size

    fun updateList(list: Collection<ColoredTravel>){
        originalList.clear()
        originalList.addAll(list)
        filteredList = originalList
    }

    // deletes item from adapter list and updates recycler
    fun remove(pos: Int) {
        originalList.removeAt(pos)
        notifyItemRemoved(pos)
    }

    override fun getFilter() : Filter {
        return object : Filter () {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()

                if (constraint == null) results.values = originalList
                else {
                    val targetType = TransportType.valueOf(constraint.toString()).ordinal
                    val filteredResults = originalList.filter { it.tipo == targetType }
                    results.values = filteredResults
                }

                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                results?.let {
                    filteredList = (it.values as List<*>).filterIsInstance<ColoredTravel>()
                    notifyDataSetChanged()
                }
            }

        }
    }

}