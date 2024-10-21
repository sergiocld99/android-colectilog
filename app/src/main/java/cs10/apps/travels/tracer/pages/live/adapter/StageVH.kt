package cs10.apps.travels.tracer.pages.live.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import cs10.apps.travels.tracer.databinding.ItemStageProgressBinding
import cs10.apps.travels.tracer.model.Parada
import cs10.apps.travels.tracer.pages.live.model.Stage
import cs10.apps.travels.tracer.utils.Utils

class StageVH(view: View) : RecyclerView.ViewHolder(view) {
    private val binding = ItemStageProgressBinding.bind(view)

    fun render(stage: Stage, travelStartTime: Int?) {
        binding.linearPbar.isIndeterminate = false
        binding.linearPbar.progress = stage.progress
        binding.startTime.text = null
        binding.endTime.text = null

        if (stage.start is Parada) binding.startLocation.text = stage.start.nombre
        if (stage.end is Parada) binding.endLocation.text = stage.end.nombre

        stage.startTime?.let {
            binding.startTime.text = Utils.hourFormat(it / 60, it % 60)
        }

        stage.endTime?.let {
            if (stage.isFinished()) binding.endTime.text = null
            else {
                val eta = Utils.hourFormat(it / 60, it % 60)
                val distance = String.format("%.1f km", stage.getLeftDistance() ?: 0.0)

                if (distance.startsWith("0")) binding.endTime.text = eta
                else binding.endTime.text = String.format("%s > %s", distance, eta)
            }
        }
    }
}