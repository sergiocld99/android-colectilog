package cs10.apps.travels.tracer.adapter

import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import cs10.apps.travels.tracer.R
import cs10.apps.travels.tracer.Utils
import cs10.apps.travels.tracer.databinding.ItemLineBinding
import cs10.apps.travels.tracer.model.joins.RatedBusLine
import cs10.apps.travels.tracer.model.lines.CustomBusLine

class LineViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val binding = ItemLineBinding.bind(view)

    fun render(customBusLine: RatedBusLine, onLineClickListener: (CustomBusLine) -> Unit){
        customBusLine.number?.let { n -> binding.title.text = "Linea $n" }
        customBusLine.name?.let { n -> binding.suggestedName.text = "Apodo: $n" }

        when(customBusLine.color){
            0 -> binding.card.setCardBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.bus))
            else -> binding.card.setCardBackgroundColor(customBusLine.color)
        }

        binding.rateText.isVisible = (customBusLine.avgUserRate > 0)
        binding.rateText.text = "${Utils.rateFormat(customBusLine.avgUserRate)} (" +
                "${customBusLine.reviewsCount} reviews)"

        binding.card.setOnClickListener { onLineClickListener(customBusLine) }
    }
}