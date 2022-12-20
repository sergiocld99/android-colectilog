package cs10.apps.travels.tracer.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import cs10.apps.common.android.NumberUtils
import cs10.apps.travels.tracer.databinding.ItemZoneBinding
import cs10.apps.travels.tracer.model.Zone

class ZoneViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val binding = ItemZoneBinding.bind(view)

    fun render(item: Zone, onLineClickListener: (Zone) -> Unit){
        binding.title.text = item.name

        val radix = 0.5 * NumberUtils.coordsDistanceToKm(item.x1 - item.x0)
        binding.radixText.text = String.format("%.2f km", radix)

        binding.speedText.text = String.format("%.4f, %.4f", item.getCenterX(), item.getCenterY())
        binding.rateText.text = String.format("%d viajes realizados", item.travelCount)

        binding.card.setOnClickListener { onLineClickListener(item) }
    }
}