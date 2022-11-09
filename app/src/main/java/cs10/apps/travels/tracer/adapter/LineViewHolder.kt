package cs10.apps.travels.tracer.adapter

import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import cs10.apps.travels.tracer.R
import cs10.apps.travels.tracer.databinding.ItemLineBinding
import cs10.apps.travels.tracer.model.lines.CustomBusLine

class LineViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val binding = ItemLineBinding.bind(view)

    fun render(customBusLine: CustomBusLine){
        customBusLine.number?.let { n -> binding.title.text = "Linea $n" }
        customBusLine.name?.let { n -> binding.suggestedName.text = "Apodo: $n" }

        val color = when(customBusLine.color){
            else -> R.color.bus
        }

        binding.card.setCardBackgroundColor(ContextCompat.getColor(binding.root.context, color))
    }
}