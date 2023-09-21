package cs10.apps.common.android

import cs10.apps.travels.tracer.enums.PrimaryDirection
import kotlin.math.abs

abstract class Localizable {
    abstract fun getX(): Double
    abstract fun getY(): Double

    fun coordsDistanceTo(other: Localizable) : Double {
        return NumberUtils.hyp(other.getX() - getX(), other.getY() - getY())
    }

    fun kmDistanceTo(other: Localizable) : Double {
        return NumberUtils.coordsDistanceToKm(coordsDistanceTo(other))
    }

    fun getPrimaryDirection(to: Localizable) : PrimaryDirection {
        val diffLatitude = to.getX() - this.getX()
        val diffLongitude = to.getY() - this.getY()

        return if (abs(diffLatitude) > abs(diffLongitude)){
            if (diffLatitude > 0) PrimaryDirection.NORTH
            else PrimaryDirection.SOUTH
        } else {
            if (diffLongitude > 0) PrimaryDirection.EAST
            else PrimaryDirection.WEST
        }
    }
}