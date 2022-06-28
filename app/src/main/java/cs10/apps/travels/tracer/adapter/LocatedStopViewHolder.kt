package cs10.apps.travels.tracer.adapter

import android.graphics.drawable.Drawable
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import cs10.apps.travels.tracer.R
import cs10.apps.travels.tracer.databinding.ItemStopBinding
import cs10.apps.travels.tracer.model.Parada

class LocatedStopViewHolder(view : View) : RecyclerView.ViewHolder(view) {

    // View Binding
    val binding = ItemStopBinding.bind(view)

    // Función para pintar el layout de este item
    fun render(parada: Parada, onClickListener: (Parada) -> Unit){
        // Rellenar textos
        binding.tvName.text = parada.nombre
        binding.tvLocation.text = binding.root.context.getString(R.string.coords, parada.latitud, parada.longitud)
        binding.tvStartCount.text = binding.root.context.getString(R.string.distance_km, parada.distanceInKm)

        // Colocar icono y color que corresponda
        val icon : Drawable?

        if (parada.tipo == 0){
            icon = AppCompatResources.getDrawable(binding.root.context, R.drawable.ic_bus)
            binding.root.background = AppCompatResources.getDrawable(binding.root.context, R.color.bus)
        } else {
            icon = AppCompatResources.getDrawable(binding.root.context, R.drawable.ic_train)
            binding.root.background = AppCompatResources.getDrawable(binding.root.context, R.color.train)
        }

        binding.ivType.setImageDrawable(icon)

        // Establecer listener para el item actual
        binding.root.setOnClickListener { onClickListener(parada) }

        // Ocultar elementos que no aplican a este holder
        binding.tvSwitcher.visibility = View.GONE
    }
}