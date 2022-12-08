package cs10.apps.travels.tracer.model.joins

import androidx.room.Ignore

data class BusRamalInfo(
    val ramal: String?,
    val color: Int,
    val avgUserRate: Double,
    val reviewsCount: Int
) {

    @Ignore
    var speed: Double? = null

    @Ignore
    var lastTravelStats: TravelStats? = null
}