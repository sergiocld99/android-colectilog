package cs10.apps.common.android

import android.location.Location
import kotlin.math.atan2

class Compass(val start: Location, val end: Localizable) {
    var currentLocation: Location = start

    fun getAngleToDestination() : Double {
        val diffLatitude = end.getX() - currentLocation.latitude
        val diffLongitude = end.getY() - currentLocation.longitude
        val angle = Math.toDegrees(atan2(diffLatitude, diffLongitude))
        return if (angle < 0) angle + 360 else angle
    }
}