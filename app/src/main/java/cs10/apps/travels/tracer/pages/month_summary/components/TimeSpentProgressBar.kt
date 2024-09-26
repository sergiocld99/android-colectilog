package cs10.apps.travels.tracer.pages.month_summary.components

import cs10.apps.travels.tracer.R
import cs10.apps.travels.tracer.databinding.ComponentLineTimeSpentBinding

class TimeSpentProgressBar(val binding: ComponentLineTimeSpentBinding) {

    fun render(lineNumber: Int, timeSpent: Int, maxTimeSpent: Int) {
        binding.tvLine.text = binding.root.context.getString(R.string.line_number, lineNumber)
        binding.pbTimeSpent.max = maxTimeSpent
        binding.pbTimeSpent.progress = timeSpent
        binding.tvTimeSpent.text = getTimeSpentInBestFormat(timeSpent)
    }

    private fun getTimeSpentInBestFormat(timeSpent: Int): String {
        val hours = timeSpent / 60
        val minutes = timeSpent % 60
        return if (hours > 0) "$hours h $minutes min" else "$minutes min"
    }
}