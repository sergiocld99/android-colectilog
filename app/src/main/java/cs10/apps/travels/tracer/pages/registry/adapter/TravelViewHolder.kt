package cs10.apps.travels.tracer.pages.registry.adapter

import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import cs10.apps.travels.tracer.R
import cs10.apps.travels.tracer.utils.Utils
import cs10.apps.travels.tracer.databinding.ItemTravelBinding
import cs10.apps.travels.tracer.common.enums.TransportType
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
            viaje.tipo == TransportType.TRAIN.ordinal -> binding.root.context.getString(R.string.train)
            viaje.tipo == TransportType.CAR.ordinal -> binding.root.context.getString(R.string.car)
            viaje.tipo == TransportType.METRO.ordinal -> binding.root.context.getString(R.string.metro)
            viaje.linea != null -> viaje.linea.toString()
            else -> ""
        }

        // details at bottom right
        val rightHighlightsVH = TravelRightHighlightsVH(binding)
        rightHighlightsVH.render(viaje)

        // COLOR
        if (viaje.color != null) binding.root.setBackgroundColor(viaje.color)
        else {
            val selectedColor = ColorUtils.colorFor(viaje.linea, viaje.tipo, viaje.nombrePdaInicio)
            binding.root.background = AppCompatResources.getDrawable(binding.root.context, selectedColor)
        }

        binding.root.setOnClickListener { onClickListener(viaje) }

        binding.root.setOnLongClickListener {
            onLongClickListener(viaje, adapterPosition)
            true
        }
    }
}