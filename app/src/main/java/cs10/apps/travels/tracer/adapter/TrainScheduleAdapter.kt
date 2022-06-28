package cs10.apps.travels.tracer.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cs10.apps.travels.tracer.R
import cs10.apps.travels.tracer.model.roca.HorarioTren

class TrainScheduleAdapter(
    var list: List<HorarioTren>,
    val onClickListener: (HorarioTren) -> Unit
) : RecyclerView.Adapter<TrainScheduleViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrainScheduleViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return TrainScheduleViewHolder(layoutInflater.inflate(R.layout.item_service, parent, false))
    }

    override fun onBindViewHolder(holder: TrainScheduleViewHolder, position: Int) {
        holder.render(list[position], position == 0 || position == itemCount-1, onClickListener)
    }

    override fun getItemCount(): Int {
        return list.size
    }


}