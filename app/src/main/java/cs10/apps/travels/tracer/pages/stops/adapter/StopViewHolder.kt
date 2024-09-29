package cs10.apps.travels.tracer.pages.stops.adapter

import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import cs10.apps.travels.tracer.R
import cs10.apps.travels.tracer.common.enums.TransportType
import cs10.apps.travels.tracer.databinding.ItemStopBinding
import cs10.apps.travels.tracer.model.Parada
import cs10.apps.travels.tracer.utils.ColorUtils

class StopViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    // View Binding
    private val binding = ItemStopBinding.bind(view)

    // Función para pintar el layout de este item
    fun render(parada: Parada, onClickListener: (Parada) -> Unit) {
        // Rellenar textos
        binding.tvName.text = parada.nombre
        binding.tvLine.text = String.format("%.1f km", parada.distance)
        binding.tvStartCount.text =
            parada.zone?.name ?: binding.root.context.getString(R.string.unknown_zone)

        binding.tvLocation.text =
            binding.root.context.getString(R.string.coords, parada.latitud, parada.longitud)

        // Colocar icono y color que corresponda
        val color: Int = when (parada.tipo) {
            TransportType.BUS.ordinal -> R.color.bus
            TransportType.TRAIN.ordinal -> R.color.train
            TransportType.CAR.ordinal -> R.color.bus_159
            TransportType.METRO.ordinal -> ColorUtils.metroColorFor(parada.nombre)
            else -> R.color.bus
        }

        binding.root.setBackgroundColor(ContextCompat.getColor(binding.root.context, color))

        // Icono según tipo de parada
        //binding.ivType.setImageDrawable(Utils.getTypeDrawable(parada.tipo, binding.root.context))
        binding.ivType.rotation = 45f - parada.angle

        // Establecer listener para el item actual
        binding.root.setOnClickListener { onClickListener(parada) }

        // Ocultar elementos que no aplican a este holder
        binding.tvSwitcher.isVisible = false
    }
}