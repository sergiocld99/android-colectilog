package cs10.apps.common.android

import android.location.Location
import kotlin.math.atan2

class Compass(val startX: Double, val startY: Double, val currentLocation: Location, val end: Localizable) {

    companion object {
        fun buildWithoutStart(currentLocation: Location, end: Localizable) : Compass {
            return Compass(currentLocation.latitude, currentLocation.longitude, currentLocation, end)
        }

        fun isForward(angle: Double) = angle in 70.0..110.0
    }

    fun getAngle(diffLatitude: Double, diffLongitude: Double) : Double {
        val angle = Math.toDegrees(atan2(diffLatitude, diffLongitude))
        return if (angle < 0) angle + 360 else angle
    }

    fun getAngleToDestination() : Double {
        val diffLatitude = end.getX() - currentLocation.latitude
        val diffLongitude = end.getY() - currentLocation.longitude
        val angleToEnd = getAngle(diffLatitude, diffLongitude)

        return if (hasMovedFromStart()) {
            val currentlyFacingAngle = getAngleFromStart()
            90.0 + (angleToEnd - currentlyFacingAngle)
        } else {
            angleToEnd
        }
    }

    fun getAngleFromStart() : Double {
        val diffLatitude = currentLocation.latitude - startX
        val diffLongitude = currentLocation.longitude - startY
        return getAngle(diffLatitude, diffLongitude)
    }

    private fun hasMovedFromStart(minimalKm: Double = 0.15) : Boolean {
        val diffLatitude = currentLocation.latitude - startX
        val diffLongitude = currentLocation.longitude - startY
        val coordsDist = NumberUtils.hyp(diffLatitude, diffLongitude)
        return NumberUtils.coordsDistanceToKm(coordsDist) > minimalKm
    }
}