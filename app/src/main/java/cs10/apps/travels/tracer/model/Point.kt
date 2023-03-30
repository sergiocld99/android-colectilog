package cs10.apps.travels.tracer.model

import android.location.Location
import cs10.apps.common.android.Localizable
import cs10.apps.common.android.NumberUtils

data class Point(private val x: Double, private val y: Double) : Localizable {

    fun getCoordsDistanceTo(location: Location) : Double {
        return NumberUtils.hyp(x - location.latitude, y - location.longitude)
    }

    fun getCoordsDistanceTo(other: Localizable) : Double {
        return NumberUtils.hyp(x - other.getX(), y - other.getY())
    }

    override fun getX(): Double {
        return x
    }

    override fun getY(): Double {
        return y
    }

}
