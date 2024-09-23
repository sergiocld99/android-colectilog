package cs10.apps.travels.tracer.pages.manage_zones.adapter

import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import cs10.apps.travels.tracer.utils.Utils
import cs10.apps.travels.tracer.databinding.ItemZoneBinding
import cs10.apps.travels.tracer.model.Zone

class ZoneViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val binding = ItemZoneBinding.bind(view)

    fun render(item: Zone, onClick: (Zone) -> Unit, onLongClick: (Zone, Int) -> Unit){
        binding.title.text = item.name

        binding.radixText.text = String.format("%.2f km", item.getRadix())
        binding.speedText.text = String.format("%.4f, %.4f", item.getCenterX(), item.getCenterY())
        binding.rateText.text = String.format("%d viajes realizados", item.zoneStats?.travelsCount?: 0)

        binding.card.setCardBackgroundColor(ContextCompat.getColor(
            binding.root.context,
            Utils.colorForType(item.zoneStats?.averageType?: 0.0)
        ))

        binding.card.setOnClickListener { onClick(item) }
        binding.card.setOnLongClickListener {
            onLongClick(item, adapterPosition)
            true
        }
    }
}