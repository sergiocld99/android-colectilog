package cs10.apps.travels.tracer.adapter

import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import cs10.apps.travels.tracer.R
import cs10.apps.travels.tracer.Utils
import cs10.apps.travels.tracer.databinding.ItemStopBinding
import cs10.apps.travels.tracer.model.ScheduledParada
import cs10.apps.travels.tracer.ui.stops.UpsideDownSwitcher

open class NextEventViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    protected val binding = ItemStopBinding.bind(view)
    private val upsideDownSwitcher = UpsideDownSwitcher()

    open fun render(item: ScheduledParada, top : Boolean, onClickListener: (ScheduledParada) -> Unit) {
        binding.tvName.text = item.nombre

        binding.ivType.setImageDrawable(Utils.getTypeDrawable(item.tipo, binding.root.context))

        binding.root.background = AppCompatResources.getDrawable(
            binding.root.context,
            Utils.colorFor(item.linea)
        )

        if (top && !item.ramal.isNullOrBlank()){
            upsideDownSwitcher.setTvSwitcher(binding.tvSwitcher)
            upsideDownSwitcher.setItem(item)
            upsideDownSwitcher.startAnimation()

            binding.tvSwitcher.isVisible = true
            binding.tvLocation.isVisible = false
        } else {
            upsideDownSwitcher.stop()
            binding.tvLocation.text = binding.root.context.getString(R.string.next_to, item.lineaAsString, item.nextArrival)

            binding.tvLocation.isVisible = true
            binding.tvSwitcher.isVisible = false
        }

        // click listener
        binding.root.setOnClickListener { onClickListener(item) }
    }
}