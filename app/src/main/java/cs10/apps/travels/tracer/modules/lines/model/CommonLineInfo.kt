package cs10.apps.travels.tracer.modules.lines.model

abstract class CommonLineInfo: Comparable<CommonLineInfo> {

    var avgUserRate = 0.0
    var reviewsCount = 0
    var speed: Double? = null

    override fun compareTo(other: CommonLineInfo): Int {
        val comp = (other.speed ?: 0.0).compareTo(speed ?: 0.0)
        if (comp == 0) return other.reviewsCount.compareTo(reviewsCount)
        return comp
    }

    abstract fun getIdentifier(): String?
    abstract fun getTypeKey(): String
}