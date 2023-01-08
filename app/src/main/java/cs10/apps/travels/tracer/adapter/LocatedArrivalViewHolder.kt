package cs10.apps.travels.tracer.adapter

import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import cs10.apps.travels.tracer.R
import cs10.apps.travels.tracer.Utils
import cs10.apps.travels.tracer.data.generator.Station
import cs10.apps.travels.tracer.databinding.ItemArrivalBinding
import cs10.apps.travels.tracer.model.Viaje
import cs10.apps.travels.tracer.model.joins.ColoredTravel
import cs10.apps.travels.tracer.model.roca.ArriboTren
import cs10.apps.travels.tracer.ui.stops.ETA_Switcher

class LocatedArrivalViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val binding = ItemArrivalBinding.bind(view)
    private val etaSwitcher = ETA_Switcher()

    fun render(viaje: ColoredTravel, top: Boolean, onClickListener: (ArriboTren) -> Unit, onDepartCallback: (Viaje) -> Unit) {

        // ramal
        if (viaje is ArriboTren) renderRamal(viaje)
        else binding.tvName.text = if (viaje.ramal == null) "" else "Ramal " + viaje.ramal

        // destination
        binding.tvStartCount.text = if (viaje.endHour != null && viaje.endMinute != null){
            binding.root.context.getString(
                R.string.destination_full,
                viaje.nombrePdaFin,
                Utils.hourFormat(viaje.endHour!!, viaje.endMinute!!)
            )
        } else {
            binding.root.context.getString(
                R.string.destination,
                viaje.nombrePdaFin
            )
        }

        // icon
        binding.ivType.setImageDrawable(Utils.getTypeDrawable(viaje.tipo, binding.root.context))

        // bg color
        if (viaje.color != null){
            // binding.root.setBackgroundColor(viaje.color)
            Utils.paintBusColor(viaje.color, binding.root)
        } else {
            // old method
            val color = if (viaje.tipo == 1 && !viaje.ramal.isNullOrEmpty() && viaje.ramal!!.contains("Directo")) R.color.bus_159
            else Utils.colorFor(viaje.linea)
            binding.root.setBackgroundColor(ContextCompat.getColor(binding.root.context, color))
        }

        // line sublabel
        binding.tvLine.text = viaje.lineSimplified

        // rate based on duration (COPIED FROM TRAVEL VIEW HOLDER)
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
        // ETA animation
        if (top && viaje is ArriboTren){
            etaSwitcher.setItem(viaje)
            etaSwitcher.setTvSwitcher(binding.tvSwitcher)
            etaSwitcher.setCallback(onDepartCallback)
            etaSwitcher.startAnimation()

            binding.tvSwitcher.isVisible = true
            binding.tvLocation.isVisible = false
        } else {
            etaSwitcher.stop()
            binding.tvLocation.text = binding.root.context.getString(
                R.string.arrival_time,
                Utils.hourFormat(viaje.startHour, viaje.startMinute)
            )

            binding.tvLocation.isVisible = true
            binding.tvSwitcher.isVisible = false
        }

        // click listener for this item
        if (viaje is ArriboTren) binding.root.setOnClickListener { onClickListener(viaje) }
    }

    private fun renderRamal(arriboTren: ArriboTren) {

        if (arriboTren.nombrePdaFin == arriboTren.nombrePdaInicio) {
            val bosques: Boolean = arriboTren.isFutureStation(Station.BOSQUES)
            val temperley: Boolean = arriboTren.isFutureStation(Station.TEMPERLEY)
            val quilmes: Boolean = arriboTren.isFutureStation(Station.QUILMES)
            val platanos: Boolean = arriboTren.isFutureStation(Station.PLATANOS)

            if (!bosques) {
                if (temperley) arriboTren.ramal = "Via Temperley"
                else if (quilmes && !platanos) arriboTren.ramal = "Via Quilmes"
            }
        }

        binding.tvName.text = arriboTren.ramal
    }
}