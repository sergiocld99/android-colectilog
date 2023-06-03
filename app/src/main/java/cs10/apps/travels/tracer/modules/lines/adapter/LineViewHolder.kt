package cs10.apps.travels.tracer.modules.lines.adapter

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

    fun render(item: RatedBusLine, onLineClickListener: (CustomBusLine) -> Unit){
        item.number?.let { n ->
            binding.title.text = if (n >= 0) "Linea $n" else "Linea Roca"
            binding.icon.setImageDrawable(ContextCompat.getDrawable(binding.root.context,
                    if (n >= 0) R.drawable.ic_bus else R.drawable.ic_train
                ))
        }

        item.name?.let { n -> binding.suggestedName.text = "- $n" }
        item.speed?.let { n -> binding.speedText.text = String.format("%.1f km/h", n) }

        Utils.paintBusColor(item.color, binding.card)

        binding.rateText.text = "${Utils.rateFormat(item.avgUserRate)} (${item.reviewsCount} reviews)"

        // visibility
        binding.suggestedName.isVisible = (item.name != null)
        binding.rateText.isVisible = (item.avgUserRate > 0)
        binding.speedText.isVisible = (item.speed != null)


        binding.card.setOnClickListener { onLineClickListener(item) }
    }
}