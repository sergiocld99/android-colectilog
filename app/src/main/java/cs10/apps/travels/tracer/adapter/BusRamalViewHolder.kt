package cs10.apps.travels.tracer.adapter

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import cs10.apps.travels.tracer.Utils
import cs10.apps.travels.tracer.databinding.ItemLineBinding
import cs10.apps.travels.tracer.model.joins.BusRamalInfo

class BusRamalViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val binding = ItemLineBinding.bind(view)

    fun render(item: BusRamalInfo, onItemClickListener: (BusRamalInfo) -> Unit){

        item.speed?.let { n -> binding.speedText.text = String.format("%.1f km/h", n) }
        Utils.paintBusColor(item.color, binding.card)

        binding.title.text = item.ramal
        binding.rateText.text = "${Utils.rateFormat(item.avgUserRate)} (${item.reviewsCount} reviews)"

        // visibility
        binding.suggestedName.isVisible = false
        binding.rateText.isVisible = (item.avgUserRate > 0)
        binding.speedText.isVisible = (item.speed != null)

        binding.card.setOnClickListener { onItemClickListener(item) }
    }
}