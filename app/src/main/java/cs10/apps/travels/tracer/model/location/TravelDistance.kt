package cs10.apps.travels.tracer.model.location

import androidx.room.Ignore
import cs10.apps.common.android.NumberUtils
import cs10.apps.travels.tracer.enums.PrimaryDirection
import kotlin.math.abs

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

    fun calculateDirection(): PrimaryDirection {
        val diffLatitude = xDiff
        val diffLongitude = yDiff

        return if (abs(diffLatitude) > abs(diffLongitude)){
            if (diffLatitude > 0) PrimaryDirection.NORTH
            else PrimaryDirection.SOUTH
        } else {
            if (diffLongitude > 0) PrimaryDirection.EAST
            else PrimaryDirection.WEST
        }
    }
}
