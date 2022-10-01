package cs10.apps.travels.tracer.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import cs10.apps.travels.tracer.Utils
import cs10.apps.travels.tracer.data.generator.Station
import cs10.apps.travels.tracer.databinding.ItemNearStopBinding
import cs10.apps.travels.tracer.model.roca.RamalSchedule

class NearStopViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    val binding = ItemNearStopBinding.bind(view)

    fun render(item: RamalSchedule) {
        binding.title.text = "${Utils.hourFormat(item.hour, item.minute)} - ${item.station.replace("Estación", "").trim()}"
        binding.description.text = item.ramal
        binding.description.isSelected = true
    }

    fun render(item: Station, destination: Station, hour: Int, minute: Int) {
        binding.title.text = item.nombre
        binding.description.text = "${Utils.hourFormat(hour, minute)} - ${destination.simplified}"
    }

    /*
    fun render(item: ScheduledParada) {
        binding.icon.setImageDrawable(Utils.getTypeDrawable(item.tipo, binding.root.context))
        binding.card.setCardBackgroundColor(ContextCompat.getColor(binding.root.context, Utils.colorFor(item.linea)))

        // title
        binding.title.text = item.nombrePdaInicio

        // description
        binding.description.text = item.nombrePdaFin + " (" + Utils.hourFormat(item.startHour, item.startMinute) + ")"
    }

     */
}