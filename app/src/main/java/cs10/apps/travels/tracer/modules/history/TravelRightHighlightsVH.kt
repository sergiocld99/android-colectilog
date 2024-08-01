package cs10.apps.travels.tracer.modules.history

import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import cs10.apps.rater.HappyRater
import cs10.apps.travels.tracer.R
import cs10.apps.travels.tracer.Utils
import cs10.apps.travels.tracer.databinding.ItemTravelBinding
import cs10.apps.travels.tracer.model.Viaje

class TravelRightHighlightsVH(private val binding: ItemTravelBinding) {

    private fun setLeftDrawable(textView: TextView, drawable: Int) {
        textView.setCompoundDrawablesWithIntrinsicBounds(
            ContextCompat.getDrawable(binding.root.context, drawable), null, null, null
        )
    }

    fun render(viaje: Viaje){
        // cost (if defined)
        binding.durationBox.isVisible = viaje.costo > 0
        binding.durationText.text = Utils.priceFormat(viaje.costo)

        binding.rateBox.isVisible = viaje.preciseRate != null

        viaje.preciseRate?.let {
            val emoji = HappyRater.getDrawableByRating(it.toInt())
            setLeftDrawable(binding.rateText, emoji)
        }
    }

    @Deprecated("UI Version until July 2024", replaceWith = ReplaceWith("render"))
    fun legacyRender(viaje: Viaje){
        // duration
        binding.durationBox.isVisible = viaje.endHour != null
        binding.durationText.text = String.format("%d'", viaje.duration)
        setLeftDrawable(binding.durationText, R.drawable.ic_timer)

        // rate
        binding.rateBox.isVisible = viaje.preciseRate != null
        binding.root.alpha = 1f

        viaje.preciseRate?.let {
            val drawable = if (it < 4) R.drawable.ic_star_half else R.drawable.ic_star
            val alpha = if (it < 3.5) 0.5f else 1f

            setLeftDrawable(binding.rateText, drawable)

            binding.root.alpha = alpha
            binding.rateText.text = Utils.rateFormat(it)
        }
    }
}