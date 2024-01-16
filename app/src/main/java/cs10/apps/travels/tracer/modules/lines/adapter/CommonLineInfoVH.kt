package cs10.apps.travels.tracer.modules.lines.adapter

import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import cs10.apps.travels.tracer.R
import cs10.apps.travels.tracer.Utils
import cs10.apps.travels.tracer.databinding.ItemLineBinding
import cs10.apps.travels.tracer.enums.TransportType
import cs10.apps.travels.tracer.modules.lines.model.BusInfo
import cs10.apps.travels.tracer.modules.lines.model.CommonLineInfo

class CommonLineInfoVH(view: View) : RecyclerView.ViewHolder(view) {
    private val binding = ItemLineBinding.bind(view)

    fun render(item: CommonLineInfo, onItemClickListener: (CommonLineInfo) -> Unit){

        item.speed?.let { n -> binding.speedText.text = String.format("%.1f km/h", n) }

        if (item is BusInfo) {
            Utils.paintBusColor(item.color, binding.card)
            binding.icon.setImageDrawable(Utils.getTypeDrawable(TransportType.BUS, binding.root.context))
        } else {
            binding.card.setCardBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.train))
            binding.icon.setImageDrawable(Utils.getTypeDrawable(TransportType.TRAIN, binding.root.context))
        }

        binding.title.text = item.getIdentifier()
        binding.rateText.text = String.format("%s (%d reviews)", Utils.rateFormat(item.correctUserRate()), item.reviewsCount)

        // visibility
        binding.suggestedName.isVisible = false
        binding.rateText.isVisible = (item.avgUserRate > 0)
        binding.speedText.isVisible = (item.speed != null)

        binding.card.setOnClickListener { onItemClickListener(item) }
    }
}