package cs10.apps.travels.tracer.model.joins

import androidx.room.Ignore

data class BusRamalInfo(
    val ramal: String?,
    val color: Int,
    val avgUserRate: Double,
    val reviewsCount: Int
) : Comparable<BusRamalInfo> {

    @Ignore
    var speed: Double? = null

    @Ignore
    var lastTravelStats: TravelStats? = null



    override fun compareTo(other: BusRamalInfo): Int {
        val comp = (other.speed ?: 0.0).compareTo(speed ?: 0.0)
        if (comp == 0) return other.reviewsCount.compareTo(reviewsCount)
        return comp
    }
}