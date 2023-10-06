package cs10.apps.travels.tracer.model.location

import androidx.room.Ignore
import cs10.apps.common.android.NumberUtils

data class TravelDistance(val id: Long, val xDiff: Double, val yDiff: Double) :
    Comparable<TravelDistance> {

    /**
     * Linear distance in kilometers
     */
    @Ignore
    val distance = NumberUtils.coordsDistanceToKm(NumberUtils.hyp(xDiff, yDiff))

    override fun compareTo(other: TravelDistance): Int {
        return distance.compareTo(other.distance)
    }
}
