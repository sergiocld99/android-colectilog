package cs10.apps.travels.tracer.model

import android.location.Location
import cs10.apps.common.android.NumberUtils

data class Point(val x: Double, val y: Double){

    fun getCoordsDistanceTo(location: Location) : Double {
        return NumberUtils.hyp(x - location.latitude, y - location.longitude)
    }

}
