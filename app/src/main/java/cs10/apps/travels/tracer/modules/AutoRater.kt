package cs10.apps.travels.tracer.modules

import cs10.apps.travels.tracer.db.ViajesDao
import cs10.apps.travels.tracer.model.Viaje

class AutoRater {

    companion object {

        fun calculateRate(travelList: List<Viaje>, dao: ViajesDao) {
            // Oct 15: calculate rate based on duration
            for (v in travelList) {
                if (v.linea == null || v.endHour == null) continue
                val minDuration = dao.getMinTravelDuration(v.linea!!, v.nombrePdaInicio, v.nombrePdaFin)
                if (minDuration == v.duration) {
                    v.preciseRate = if (v.rate == null) null else v.rate.toDouble()
                } else {
                    val rate = 5.0 * minDuration / v.duration
                    v.addPreciseRate(rate)
                }
            }
        }
    }
}