package cs10.apps.common.android

import android.location.Location
import java.util.concurrent.TimeUnit

class TimedLocation(val location: Location, val timestamp: Long){

    companion object {
        fun createCurrentObject(location: Location) : TimedLocation {
            return TimedLocation(location, System.currentTimeMillis())
        }

        fun isStillValid(timedLocation: TimedLocation) : Boolean {
            return timedLocation.timestamp > (System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(1))
        }
    }
}