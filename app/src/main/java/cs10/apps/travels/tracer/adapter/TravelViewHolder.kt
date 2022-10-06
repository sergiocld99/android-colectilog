package cs10.apps.travels.tracer.adapter

import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import cs10.apps.travels.tracer.Utils
import cs10.apps.travels.tracer.databinding.ItemTravelBinding
import cs10.apps.travels.tracer.model.Viaje

class TravelViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val binding = ItemTravelBinding.bind(view)

    fun render(viaje: Viaje, position: Int, onClickListener: (Viaje) -> Unit, onLongClickListener: (Viaje, Int) -> Unit) {
        binding.ivType.setImageDrawable(Utils.getTypeDrawable(viaje.tipo, binding.root.context))
        binding.tvDatetime.text = viaje.startTimeString
        binding.tvLine.text = viaje.ramalAndPrice

        // start and end
        binding.tvStartPlace.text = viaje.startAndEnd
        binding.tvStartPlace.isSelected = position == 0

        // line sublabel
        binding.tvLineNumber.text = if (viaje.linea == null) "Roca"
        else viaje.linea.toString()

        binding.root.background = AppCompatResources.getDrawable(binding.root.context, Utils.colorFor(viaje.linea))
        binding.root.setOnClickListener { onClickListener(viaje) }

        binding.root.setOnLongClickListener {
            onLongClickListener(viaje, adapterPosition)
            true
        }
    }
}