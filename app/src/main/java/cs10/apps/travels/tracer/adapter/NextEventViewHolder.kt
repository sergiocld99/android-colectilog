package cs10.apps.travels.tracer.adapter

import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import cs10.apps.travels.tracer.R
import cs10.apps.travels.tracer.Utils
import cs10.apps.travels.tracer.databinding.ItemArrivalBinding
import cs10.apps.travels.tracer.enums.TransportType
import cs10.apps.travels.tracer.model.joins.ScheduledParada
import cs10.apps.travels.tracer.ui.stops.UpsideDownSwitcher

open class NextEventViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    protected val binding = ItemArrivalBinding.bind(view)
    private val upsideDownSwitcher = UpsideDownSwitcher()

    open fun render(item: ScheduledParada, top : Boolean, onClickListener: (ScheduledParada) -> Unit) {
        binding.tvName.text = item.nombre

        val type = if (item.linea == null) TransportType.TRAIN else TransportType.BUS
        binding.ivType.setImageDrawable(Utils.getTypeDrawable(type, binding.root.context))
        binding.tvLine.text = item.lineaAsString

        when(type){
            TransportType.BUS -> Utils.paintBusColor(item.color, binding.root)
            else -> binding.root.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.train))
        }

        if (top && !item.ramal.isNullOrBlank()){
            upsideDownSwitcher.setTvSwitcher(binding.tvSwitcher)
            upsideDownSwitcher.setItem(item)
            upsideDownSwitcher.startAnimation()

            binding.tvSwitcher.isVisible = true
            binding.tvLocation.isVisible = false
        } else {
            upsideDownSwitcher.stop()
            binding.tvLocation.text = binding.root.context.getString(R.string.arrives_at, item.nextArrival)

            binding.tvLocation.isVisible = true
            binding.tvSwitcher.isVisible = false
        }

        // click listener
        binding.root.setOnClickListener { onClickListener(item) }
    }
}