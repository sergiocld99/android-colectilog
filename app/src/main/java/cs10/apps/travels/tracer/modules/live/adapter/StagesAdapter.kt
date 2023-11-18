package cs10.apps.travels.tracer.modules.live.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cs10.apps.travels.tracer.R
import cs10.apps.travels.tracer.modules.live.model.Stage

class StagesAdapter(val data: MutableList<Stage> = mutableListOf()) : RecyclerView.Adapter<StageVH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StageVH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_stage_progress, parent, false)
        return StageVH(v)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: StageVH, position: Int) {
        holder.render(data[position], data.firstOrNull()?.startTime)
    }
}

