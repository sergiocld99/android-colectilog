package cs10.apps.travels.tracer.domain

import cs10.apps.travels.tracer.db.ViajesDao

class GetMaxTravelDistanceUseCase(private val viajesDao: ViajesDao) {

    operator fun invoke(): Double? {
        val list = viajesDao.travelDistances

        return if (list.isNullOrEmpty()) null
        else {
            list.sort()
            list.last().distance
        }
    }
}