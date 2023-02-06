package cs10.apps.travels.tracer.ui.live

import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import cs10.apps.travels.tracer.R
import cs10.apps.travels.tracer.databinding.ContentLiveWaitingBinding
import cs10.apps.travels.tracer.model.Parada

class LiveWaitingView(private val binding: ContentLiveWaitingBinding) {
    private var expanded = false
    private var enabled = false

    init {
        disableAnimation()
    }

    private fun animateButton(){
        if (expanded) animateExpandButton()
        else animateReduceButton()
    }

    private fun animateExpandButton(){
        binding.circularCard.animate()
            .scaleX(1.15f)
            .scaleY(1.15f)
            .setDuration(1000)
            .withEndAction {
                expanded = true
                if (enabled) animateReduceButton()
            }.start()
    }

    private fun animateReduceButton(){
        binding.circularCard.animate()
            .scaleX(0.8f)
            .scaleY(0.8f)
            .setDuration(1000)
            .withEndAction {
                expanded = false
                if (enabled) animateExpandButton()
            }.start()
    }

    fun hide(){
        binding.root.isVisible = false
    }

    fun show(){
        binding.root.isVisible = true
    }

    fun setVisibility(b: Boolean) {
        if (b) show()
        else hide()
    }

    fun setStopHere(stopHere: Parada?){
        when (stopHere) {
            null -> {
                binding.currentStopTv.text = null
                disableAnimation()
            } else -> {
                binding.currentStopTv.text = "Esperando en ${stopHere.nombre}"
                enableAnimation()
            }
        }
    }

    private fun disableAnimation(){
        enabled = false
        binding.circularCard.setCardBackgroundColor(ContextCompat.getColor(
            binding.root.context, android.R.color.darker_gray))
    }

    private fun enableAnimation(){
        if (!enabled){
            enabled = true
            binding.circularCard.setCardBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.bus))
            animateButton()
        }
    }

    fun isVisible() : Boolean {
        return binding.root.isVisible
    }
}