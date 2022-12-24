package cs10.apps.travels.tracer.domain

import cs10.apps.common.android.Calendar2
import cs10.apps.travels.tracer.Utils
import cs10.apps.travels.tracer.db.MiDB
import cs10.apps.travels.tracer.model.joins.ColoredTravel

class GetCurrentTravelUseCase(private val db: MiDB) {

    operator fun invoke(): ColoredTravel? {
        val (y, m, d) = Calendar2.getDate()
        val currentTs = Utils.getCurrentTs()
        return db.viajesDao().getCurrentTravel(y, m, d, currentTs)
    }
}