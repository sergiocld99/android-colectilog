package cs10.apps.travels.tracer.pages.manage_lines.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cs10.apps.travels.tracer.R
import cs10.apps.travels.tracer.pages.manage_lines.model.CommonLineInfo

class CommonLineInfoAdapter(
    var list: List<CommonLineInfo>,
    private val onItemClick: (CommonLineInfo) -> Unit
) : RecyclerView.Adapter<CommonLineInfoVH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommonLineInfoVH {
        val layoutInflater = LayoutInflater.from(parent.context)
        return CommonLineInfoVH(layoutInflater.inflate(R.layout.item_line, parent, false))
    }

    override fun onBindViewHolder(holder: CommonLineInfoVH, position: Int) {
        holder.render(list[position], onItemClick)
    }

    override fun getItemCount() = list.size

}
