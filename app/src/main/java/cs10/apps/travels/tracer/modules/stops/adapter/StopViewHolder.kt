package cs10.apps.travels.tracer.modules.stops.adapter

import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import cs10.apps.travels.tracer.R
import cs10.apps.travels.tracer.Utils
import cs10.apps.travels.tracer.databinding.ItemStopBinding
import cs10.apps.travels.tracer.enums.TransportType
import cs10.apps.travels.tracer.model.Parada

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

        /*
         if (adapterPosition == 0) {
             val diff = Utils.bestRound(parada.doubleHistory.diffSum)
             binding.tvLocation.text = when {
                parada.doubleHistory.currentIsGreater() -> "Te alejaste $diff metros"
                else -> "Te acercaste " + (-diff) + " metros"
             }

         */

        binding.tvLocation.text =
            binding.root.context.getString(R.string.coords, parada.latitud, parada.longitud)

        // Colocar icono y color que corresponda
        val color: Int = when (parada.tipo) {
            TransportType.BUS.ordinal -> R.color.bus
            TransportType.TRAIN.ordinal -> R.color.train
            TransportType.CAR.ordinal -> R.color.bus_159
            TransportType.METRO.ordinal -> {
                if (parada.nombre.endsWith("(A)")) R.color.bus
                else if (parada.nombre.endsWith("(B)")) R.color.bus_414
                else if (parada.nombre.endsWith("(C)")) R.color.train
                else if (parada.nombre.endsWith("(D)")) R.color.bus_500
                else if (parada.nombre.endsWith("(E)")) R.color.purple_500
                else if (parada.nombre.endsWith("(H)")) R.color.bus_148
                else R.color.black
            }
            else -> R.color.bus
        }

        binding.root.setBackgroundColor(ContextCompat.getColor(binding.root.context, color))

        // Icono según tipo de parada
        binding.ivType.setImageDrawable(Utils.getTypeDrawable(parada.tipo, binding.root.context))

        // Establecer listener para el item actual
        binding.root.setOnClickListener { onClickListener(parada) }

        // Ocultar elementos que no aplican a este holder
        binding.tvSwitcher.isVisible = false
    }
}