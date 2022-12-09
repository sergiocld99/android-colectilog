package cs10.apps.travels.tracer.model.joins

import androidx.room.Ignore
import cs10.apps.travels.tracer.model.lines.CustomBusLine

class RatedBusLine(
    id: Int,
    number: Int?,
    name: String?,
    color: Int,
    val avgUserRate: Double,
    val reviewsCount: Int
) : CustomBusLine(id, number, name, color) {

    @Ignore
    var speed: Double? = null

    override fun compareTo(other: CustomBusLine): Int {

        if (other is RatedBusLine){
            val comp = other.reviewsCount.compareTo(this.reviewsCount)
            if (comp != 0) return comp
        }

        return super.compareTo(other)
    }

}