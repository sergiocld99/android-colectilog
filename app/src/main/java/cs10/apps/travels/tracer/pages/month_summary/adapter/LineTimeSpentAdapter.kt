package cs10.apps.travels.tracer.pages.month_summary.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cs10.apps.travels.tracer.databinding.ComponentLineTimeSpentBinding
import cs10.apps.travels.tracer.pages.month_summary.components.TimeSpentProgressBar
import cs10.apps.travels.tracer.pages.month_summary.model.TimeLineStat

class LineTimeSpentAdapter : RecyclerView.Adapter<LineTimeSpentAdapter.ViewHolder>() {
    private val lineStats = mutableListOf<TimeLineStat>()
    private var maxTimeSpent = 0

    fun updateData(newLineStats: List<TimeLineStat>) {
        lineStats.clear()
        lineStats.addAll(newLineStats)
        maxTimeSpent = lineStats.sumOf { it.timeSpent }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ComponentLineTimeSpentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val lineStat = lineStats[position]
        holder.render(lineStat.lineNumber, lineStat.timeSpent, maxTimeSpent)
    }

    override fun getItemCount(): Int {
        return lineStats.size
    }

    class ViewHolder(private val binding: ComponentLineTimeSpentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun render(lineNumber: Int, timeSpent: Int, maxTimeSpent: Int) {
            TimeSpentProgressBar(binding).render(lineNumber, timeSpent, maxTimeSpent)
        }
    }
}