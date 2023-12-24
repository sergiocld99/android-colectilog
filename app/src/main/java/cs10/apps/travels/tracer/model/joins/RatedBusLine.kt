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

    fun correctUserRate(): Double {
        return if (reviewsCount > 4) avgUserRate
        else {
            val aux = avgUserRate * reviewsCount + 3.0 * (5-reviewsCount)
            aux / 5
        }
    }

    override fun compareTo(other: CustomBusLine): Int {

        if (other is RatedBusLine){
            var comp = (other.correctUserRate() ?: 0.0).compareTo(this.correctUserRate() ?: 0.0)

            if (comp == 0) comp = other.reviewsCount.compareTo(this.reviewsCount)
            if (comp != 0) return comp
        }

        return super.compareTo(other)
    }

}