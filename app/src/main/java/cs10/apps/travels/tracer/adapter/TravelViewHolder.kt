package cs10.apps.travels.tracer.adapter

import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import cs10.apps.travels.tracer.R
import cs10.apps.travels.tracer.Utils
import cs10.apps.travels.tracer.databinding.ItemTravelBinding
import cs10.apps.travels.tracer.enums.TransportType
import cs10.apps.travels.tracer.model.Viaje
import cs10.apps.travels.tracer.model.joins.ColoredTravel
import cs10.apps.travels.tracer.utils.ColorUtils

class TravelViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val binding = ItemTravelBinding.bind(view)

    fun render(
        viaje: ColoredTravel,
        onClickListener: (Viaje) -> Unit,
        onLongClickListener: (Viaje, Int) -> Unit
    ) {
        binding.ivType.setImageDrawable(Utils.getTypeDrawable(viaje.tipo, binding.root.context))
        binding.tvDatetime.text = viaje.startTimeString
        binding.tvLine.text = viaje.ramal

        // start and end
        binding.tvStartPlace.text = viaje.startAndEnd
        binding.tvStartPlace.isSelected = adapterPosition == 0

        // line sublabel
        binding.tvLineNumber.text = when {
            viaje.tipo == TransportType.TRAIN.ordinal -> "Roca"
            viaje.tipo == TransportType.CAR.ordinal -> "Auto"
            viaje.linea != null -> viaje.linea.toString()
            else -> ""
        }

        // duration
        binding.durationBox.isVisible = viaje.endHour != null
        binding.durationText.text = String.format("%d'", viaje.duration)

        // rate
        binding.rateBox.isVisible = viaje.preciseRate != null
        binding.root.alpha = 1f

        viaje.preciseRate?.let {
            val drawable = if (it < 4) R.drawable.ic_star_half else R.drawable.ic_star
            val alpha = if (it < 3.5) 0.5f else 1f

            binding.rateText.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(binding.root.context, drawable), null, null, null
            )

            binding.root.alpha = alpha
            binding.rateText.text = Utils.rateFormat(it)
        }

        // COLOR
        if (viaje.color != null) binding.root.setBackgroundColor(viaje.color)
        else {
            val selectedColor = ColorUtils.colorFor(viaje.linea, viaje.tipo)
            binding.root.background = AppCompatResources.getDrawable(binding.root.context, selectedColor)
        }

        binding.root.setOnClickListener { onClickListener(viaje) }

        binding.root.setOnLongClickListener {
            onLongClickListener(viaje, adapterPosition)
            true
        }
    }
}