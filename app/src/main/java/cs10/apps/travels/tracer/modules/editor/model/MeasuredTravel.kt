package cs10.apps.travels.tracer.modules.editor.model

import cs10.apps.travels.tracer.enums.PrimaryDirection
import cs10.apps.travels.tracer.model.Viaje

class MeasuredTravel(val viaje: Viaje, val distanceKm: Double, val direction: PrimaryDirection) {

    fun getSpeedKmH() : Double {
        val km = distanceKm
        val h = viaje.duration / 60.0
        return if (h > 0) km/h else 0.0
    }
}