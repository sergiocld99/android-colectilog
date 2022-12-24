package cs10.apps.travels.tracer.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cs10.apps.travels.tracer.R
import cs10.apps.travels.tracer.model.Parada

class StopAdapter (
    var paradasList: List<Parada>,
    private val onClickListener: (Parada) -> Unit
) : RecyclerView.Adapter<StopViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StopViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return StopViewHolder(layoutInflater.inflate(R.layout.item_stop, parent, false))
    }

    override fun onBindViewHolder(holder: StopViewHolder, position: Int) {
        val item = paradasList[position]
        holder.render(item, onClickListener)
    }

    override fun getItemCount() = paradasList.size
}