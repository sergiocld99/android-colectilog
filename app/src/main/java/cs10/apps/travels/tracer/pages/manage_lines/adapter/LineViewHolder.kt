package cs10.apps.travels.tracer.pages.manage_lines.adapter

import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import cs10.apps.travels.tracer.R
import cs10.apps.travels.tracer.databinding.ItemLineBinding
import cs10.apps.travels.tracer.model.joins.RatedBusLine
import cs10.apps.travels.tracer.model.lines.CustomBusLine
import cs10.apps.travels.tracer.utils.Utils

class LineViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val binding = ItemLineBinding.bind(view)

    fun render(item: RatedBusLine, onLineClickListener: (CustomBusLine) -> Unit){
        binding.title.text = ""
        binding.suggestedName.text = ""
        binding.rateText.text = ""
        binding.speedText.text = ""
        binding.card.setCardBackgroundColor(null)

        item.number?.let { n ->
            binding.title.text = if (n >= 0) "Linea $n" else item.name
            binding.icon.setImageDrawable(ContextCompat.getDrawable(binding.root.context,
                    if (n >= 0) R.drawable.ic_bus
                    else if (item.id == -1) R.drawable.ic_train
                    else if (item.id == -2) R.drawable.ic_car
                    else R.drawable.ic_railway
                ))
        }

        if (item.number != -1) item.name?.let { n -> binding.suggestedName.text = "- $n" }
        item.speed?.let { n -> binding.speedText.text = String.format("%.1f km/h", n) }

        if (item.id >= 0 || item.color == 0) Utils.paintBusColor(item.color, binding.card)
        else binding.card.setCardBackgroundColor(ContextCompat.getColor(binding.root.context, item.color))

        binding.rateText.text = "${Utils.rateFormat(item.correctUserRate())} (${item.reviewsCount} reviews)"

        // visibility
        binding.suggestedName.isVisible = (item.name != null)
        binding.rateText.isVisible = (item.avgUserRate > 0)
        binding.speedText.isVisible = (item.speed != null)


        binding.card.setOnClickListener { onLineClickListener(item) }
    }
}