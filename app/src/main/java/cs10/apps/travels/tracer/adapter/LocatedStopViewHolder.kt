package cs10.apps.travels.tracer.adapter

import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import cs10.apps.travels.tracer.R
import cs10.apps.travels.tracer.Utils
import cs10.apps.travels.tracer.databinding.ItemStopBinding
import cs10.apps.travels.tracer.model.Parada

class LocatedStopViewHolder(view : View) : RecyclerView.ViewHolder(view) {

    // View Binding
    private val binding = ItemStopBinding.bind(view)

    // Función para pintar el layout de este item
    fun render(parada: Parada, top: Boolean, onClickListener: (Parada) -> Unit){
        // Rellenar textos
        binding.tvName.text = parada.nombre
        binding.tvStartCount.text = binding.root.context.getString(R.string.distance_km, parada.distanceInKm)

         if (top) {
             val diff = Utils.bestRound(parada.doubleHistory.diffSum)
             binding.tvLocation.text = when {
                parada.doubleHistory.currentIsGreater() -> "Te alejaste $diff metros"
                else -> "Te acercaste " + (-diff) + " metros"
             }
        } else binding.tvLocation.text = binding.root.context.getString(R.string.coords, parada.latitud, parada.longitud)

        // Colocar icono y color que corresponda
        val color : Int

        if (parada.tipo == 0){
            color = when {
                parada.doubleHistory.currentIsLess() -> R.color.bus_500
                parada.doubleHistory.currentIsGreater() -> R.color.bus_414
                else -> R.color.bus
            }

            binding.root.setBackgroundColor(ContextCompat.getColor(binding.root.context, color))
        } else {
            color = when {
                parada.doubleHistory.currentIsLess() -> R.color.bus_159
                parada.doubleHistory.currentIsGreater() -> R.color.bus_324
                else -> R.color.train
            }

            binding.root.setBackgroundColor(ContextCompat.getColor(binding.root.context, color))
        }

        // Icono según tipo de parada
        binding.ivType.setImageDrawable(Utils.getTypeDrawable(parada.tipo, binding.root.context))

        // Establecer listener para el item actual
        binding.root.setOnClickListener { onClickListener(parada) }

        // Ocultar elementos que no aplican a este holder
        binding.tvSwitcher.isVisible = false
    }
}