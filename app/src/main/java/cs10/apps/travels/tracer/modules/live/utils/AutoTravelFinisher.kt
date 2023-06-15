package cs10.apps.travels.tracer.modules.live.utils

import cs10.apps.common.android.Localizable
import cs10.apps.travels.tracer.Utils

class AutoTravelFinisher {

    fun evaluate(start: Localizable, end: Localizable, currentDistanceToEnd: Double) : Boolean {

        val totalDistance = start.kmDistanceTo(end)

        return when (Utils.getDirection(start, end)) {
            Utils.Direction.SOUTH_WEST -> f0SW(currentDistanceToEnd, totalDistance)
            Utils.Direction.SOUTH_EAST -> f0SE(currentDistanceToEnd, totalDistance)
            Utils.Direction.NORTH_WEST -> f0NW(currentDistanceToEnd, totalDistance)
            else -> f0NE(currentDistanceToEnd, totalDistance)
        }
    }

    private fun f0SW(currentDistanceToEnd: Double, totalDistance: Double): Boolean {
        val distanceToEndNeeded = 0.084 * totalDistance       // 500 -> 300m to Home
        return currentDistanceToEnd < distanceToEndNeeded
    }

    private fun f0SE(currentDistanceToEnd: Double, totalDistance: Double): Boolean {
        val distanceToEndNeeded = 0.024 * totalDistance       // 338 -> 750m to Av. 1 y 48
        return currentDistanceToEnd < distanceToEndNeeded
    }

    private fun f0NW(currentDistanceToEnd: Double, totalDistance: Double): Boolean {
        val distanceToEndNeeded = 0.036 * totalDistance       // 338 -> 1160m to Cruce Varela
        return currentDistanceToEnd < distanceToEndNeeded     // LGR -> 300m to Est Adrogue
    }

    private fun f0NE(currentDistanceToEnd: Double, totalDistance: Double): Boolean {
        val distanceToEndNeeded = 0.12 * totalDistance       // 500 -> 450m to Cruce Varela
        return currentDistanceToEnd < distanceToEndNeeded
    }
}