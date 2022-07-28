package cs10.apps.travels.tracer.adapter

import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import cs10.apps.travels.tracer.R
import cs10.apps.travels.tracer.Utils
import cs10.apps.travels.tracer.databinding.ItemServiceBinding
import cs10.apps.travels.tracer.model.roca.HorarioTren

class TrainScheduleViewHolder(view : View) : RecyclerView.ViewHolder(view) {

    private val binding = ItemServiceBinding.bind(view)

    fun render(item: HorarioTren, cabecera: Boolean, current: Boolean, onClickListener: (HorarioTren) -> Unit){

        binding.tvStation.text = item.station
        binding.tvArrivalTime.text = Utils.hourFormat(item.hour, item.minute)
        binding.tvCombination.text = if (item.combination == null){
            binding.root.context.getString(R.string.train_price, Utils.priceFormat(item.tarifa))
        } else {
            binding.root.context.getString(R.string.combination_info, item.combinationRamal, Utils.hourFormat(item.combination.hour, item.combination.minute))
        }

        // card background color
        val color = when {
            cabecera -> R.color.purple_700
            current -> R.color.bus_159
            item.combination != null -> R.color.bus_324
            item.service == 0L -> android.R.color.transparent
            else -> R.color.bus
        }

        binding.trainIcon.isVisible = current
        binding.root.setBackgroundColor(ContextCompat.getColor(binding.root.context, color))
        binding.root.setOnClickListener { onClickListener(item) }
    }

}