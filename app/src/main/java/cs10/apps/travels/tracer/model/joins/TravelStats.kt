package cs10.apps.travels.tracer.model.joins

import cs10.apps.common.android.NumberUtils

data class TravelStats(
    val start_x: Double,
    val start_y: Double,
    val end_x: Double,
    val end_y: Double,
    val start_time: Int,
    val end_time: Int
) {

    fun calculateCoordsDistance() : Double {
        return NumberUtils.hyp(end_x - start_x, end_y - start_y)
    }

    fun calculateDistanceInKm() : Double {
        return NumberUtils.coordsDistanceToKm(calculateCoordsDistance())
    }

    fun calculateDurationInHours() : Double {
        return NumberUtils.minutesToHours(end_time - start_time)
    }

    fun calculateSpeedInKmH() : Double {
        return calculateDistanceInKm() / calculateDurationInHours()
    }
}