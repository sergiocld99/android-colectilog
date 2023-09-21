package cs10.apps.travels.tracer.modules.live.model

import cs10.apps.common.android.Localizable
import cs10.apps.common.android.NumberUtils
import cs10.apps.travels.tracer.enums.PrimaryDirection
import kotlin.math.abs
import kotlin.math.roundToInt

data class Stage(val start: Localizable, val end: Localizable, var progress: Int = 0){
    private val coordsDistance = end.coordsDistanceTo(start)
    val kmDistance = end.kmDistanceTo(start)
    val primaryDirection = start.getPrimaryDirection(end)
    var startTime: Int? = null
    var endTime: Int? = null

    fun updateProgressFor(other: Localizable) {
        val d1 = other.coordsDistanceTo(start)
        val d2 = other.coordsDistanceTo(end)
        progress = (100 * d1 / (d1+d2)).roundToInt()
    }

    fun contains(other: Localizable) : Boolean {
        return when(primaryDirection){
            PrimaryDirection.NORTH -> NumberUtils.between(other.getX(), start.getX(), end.getX())
            PrimaryDirection.SOUTH -> NumberUtils.between(other.getX(), end.getX(), start.getX())
            PrimaryDirection.EAST -> NumberUtils.between(other.getY(), start.getY(), end.getY())
            PrimaryDirection.WEST -> NumberUtils.between(other.getY(), end.getY(), start.getY())
        }
    }

    fun deviation(other: Localizable) : Double {
        val d1 = other.coordsDistanceTo(start)
        val d2 = other.coordsDistanceTo(end)
        return abs((d1+d2) - coordsDistance)
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
}