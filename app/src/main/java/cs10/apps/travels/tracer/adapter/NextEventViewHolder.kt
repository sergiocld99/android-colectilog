package cs10.apps.travels.tracer.adapter

import android.view.View
import androidx.appcompat.content.res.AppCompatResources
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

        val icon = if (item.tipo == 0) R.drawable.ic_bus
        else R.drawable.ic_train

        binding.ivType.setImageDrawable(AppCompatResources.getDrawable(binding.root.context, icon))

        binding.root.background = AppCompatResources.getDrawable(
            binding.root.context,
            Utils.colorFor(item.linea)
        )

        if (top && item.ramal != null){
            upsideDownSwitcher.setTvSwitcher(binding.tvSwitcher)
            upsideDownSwitcher.setItem(item)
            upsideDownSwitcher.startAnimation()

            binding.tvSwitcher.visibility = View.VISIBLE
            binding.tvLocation.visibility = View.GONE
        } else {
            upsideDownSwitcher.stop()
            binding.tvLocation.text = binding.root.context.getString(R.string.next_to, item.lineaAsString, item.nextArrival)

            binding.tvLocation.visibility = View.VISIBLE
            binding.tvSwitcher.visibility = View.GONE
        }

        // click listener
        binding.root.setOnClickListener { onClickListener(item) }
    }
}