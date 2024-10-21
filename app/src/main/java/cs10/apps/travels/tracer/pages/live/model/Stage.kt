package cs10.apps.travels.tracer.pages.live.model

import cs10.apps.common.android.Localizable
import cs10.apps.common.android.NumberUtils
import cs10.apps.travels.tracer.common.enums.PrimaryDirection
import kotlin.math.roundToInt

/**
 * @param progress between 0 and 100
 */
data class Stage(val start: Localizable, val end: Localizable, var progress: Int = 0){
    val kmDistance = end.kmDistanceTo(start)
    private var kmDistanceFromCurrent: Double? = null

    val primaryDirection = start.getPrimaryDirection(end)
    var startTime: Int? = null
    var endTime: Int? = null

    fun updateProgressFor(other: Localizable) {
        val d1 = other.coordsDistanceTo(start)
        val d2 = other.coordsDistanceTo(end)
        val unitProgress = d1 / (d1+d2)
        kmDistanceFromCurrent = kmDistance * (1.0 - unitProgress)
        progress = (100 * unitProgress).roundToInt()
    }

    fun contains(other: Localizable) : Boolean {
        return when(primaryDirection){
            PrimaryDirection.NORTH -> NumberUtils.between(other.getX(), start.getX(), end.getX())
            PrimaryDirection.SOUTH -> NumberUtils.between(other.getX(), end.getX(), start.getX())
            PrimaryDirection.EAST -> NumberUtils.between(other.getY(), start.getY(), end.getY())
            PrimaryDirection.WEST -> NumberUtils.between(other.getY(), end.getY(), start.getY())
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other is Stage){
            return other.start == start && other.end == end
        }

        return super.equals(other)
    }

    override fun hashCode(): Int {
        return end.hashCode() - start.hashCode()
    }

    fun isFinished() = progress == 100

    fun isStarted() = progress > 0

    fun getLeftDistance(): Double? {
        return if (!isStarted()) kmDistance
        else kmDistanceFromCurrent
    }
}