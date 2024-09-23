package cs10.apps.travels.tracer.pages.manage_lines.model

abstract class CommonLineInfo: Comparable<CommonLineInfo> {

    var avgUserRate = 0.0
    var reviewsCount = 0
    var speed: Double? = null

    fun correctUserRate(): Double {
        return if (reviewsCount > 4) avgUserRate
        else {
            val aux = avgUserRate * reviewsCount + 3.0 * (5-reviewsCount)
            aux / 5
        }
    }

    override fun compareTo(other: CommonLineInfo): Int {
        val comp = other.correctUserRate().compareTo(this.correctUserRate())
        if (comp == 0) return other.reviewsCount.compareTo(reviewsCount)
        return comp
    }

    abstract fun getIdentifier(): String?
    abstract fun getTypeKey(): String
}