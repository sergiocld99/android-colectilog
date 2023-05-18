package cs10.apps.travels.tracer.model.joins

open class BusInfo: Comparable<BusInfo> {
    var color = 0
    var avgUserRate = 0.0
    var reviewsCount = 0
    var speed: Double? = null

    override fun compareTo(other: BusInfo): Int {
        val comp = (other.speed ?: 0.0).compareTo(speed ?: 0.0)
        if (comp == 0) return other.reviewsCount.compareTo(reviewsCount)
        return comp
    }
}