package cs10.apps.travels.tracer.model.location

import androidx.room.Ignore
import kotlin.math.sqrt

data class TravelDistance(val id: Long, val xDiff: Double, val yDiff: Double) :
    Comparable<TravelDistance> {

    @Ignore
    val distance = sqrt(xDiff * xDiff + yDiff * yDiff) * 91.97

    override fun compareTo(other: TravelDistance): Int {
        return distance.compareTo(other.distance)
    }
}
