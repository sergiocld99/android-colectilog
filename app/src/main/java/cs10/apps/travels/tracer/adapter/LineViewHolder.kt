package cs10.apps.travels.tracer.adapter

import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import cs10.apps.travels.tracer.R
import cs10.apps.travels.tracer.databinding.ItemLineBinding
import cs10.apps.travels.tracer.model.lines.CustomBusLine

class LineViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val binding = ItemLineBinding.bind(view)

    fun render(customBusLine: CustomBusLine, onLineClickListener: (CustomBusLine) -> Unit){
        customBusLine.number?.let { n -> binding.title.text = "Linea $n" }
        customBusLine.name?.let { n -> binding.suggestedName.text = "Apodo: $n" }

        when(customBusLine.color){
            0 -> binding.card.setCardBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.bus))
            else -> binding.card.setCardBackgroundColor(customBusLine.color)
        }

        /*
        val color = when(customBusLine.color){
            0 -> R.color.bus
            else -> customBusLine.color
        }

        binding.card.setCardBackgroundColor(ContextCompat.getColor(binding.root.context, color))

         */
        binding.card.setOnClickListener { onLineClickListener(customBusLine) }
    }
}