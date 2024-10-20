package cs10.apps.travels.tracer.pages.registry.editor.components

import cs10.apps.travels.tracer.R
import cs10.apps.travels.tracer.databinding.EditorTopCardBinding
import cs10.apps.travels.tracer.pages.registry.editor.model.MeasuredTravel

class EditorTopCard(val binding: EditorTopCardBinding) {
    private fun getString(resId: Int, vararg args: Any) = binding.root.context.getString(resId, *args)

    fun render(mt: MeasuredTravel){
        binding.distanceText.text = getString(R.string.total_distance_km, mt.distanceKm)
        binding.speedText.text = getString(R.string.speed_kmh, mt.getSpeedKmH())
        binding.directionText.text = getString(R.string.direction, mt.direction.legibleString(binding.root.context))
    }
}
