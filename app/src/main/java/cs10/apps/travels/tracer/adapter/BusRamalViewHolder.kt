package cs10.apps.travels.tracer.adapter

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import cs10.apps.travels.tracer.Utils
import cs10.apps.travels.tracer.databinding.ItemLineBinding
import cs10.apps.travels.tracer.model.joins.BusDestinationInfo
import cs10.apps.travels.tracer.model.joins.BusInfo
import cs10.apps.travels.tracer.model.joins.BusRamalInfo

class BusRamalViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val binding = ItemLineBinding.bind(view)

    fun render(item: BusInfo, onItemClickListener: (BusInfo) -> Unit){

        item.speed?.let { n -> binding.speedText.text = String.format("%.1f km/h", n) }
        Utils.paintBusColor(item.color, binding.card)

        if (item is BusRamalInfo) binding.title.text = item.ramal
        else if (item is BusDestinationInfo) binding.title.text = item.nombrePdaFin

        binding.rateText.text = String.format("%s (%d reviews)",
            Utils.rateFormat(item.avgUserRate), item.reviewsCount)

        // visibility
        binding.suggestedName.isVisible = false
        binding.rateText.isVisible = (item.avgUserRate > 0)
        binding.speedText.isVisible = (item.speed != null)

        binding.card.setOnClickListener { onItemClickListener(item) }
    }
}