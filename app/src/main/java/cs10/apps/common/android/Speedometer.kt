package cs10.apps.common.android

import android.location.Location

class Speedometer {
    private var currentLocation : TimedLocation? = null
    private var previousLocation : TimedLocation? = null
    private var currentSpeedInKmH : Double? = null

    fun update(currentLocation: TimedLocation) : Double? {
        updateVariables(currentLocation)
        currentSpeedInKmH = calculateSpeedInKmH()
        return currentSpeedInKmH
    }

    private fun updateVariables(currentLocation: TimedLocation){
        previousLocation = this.currentLocation
        this.currentLocation = currentLocation
    }

    private fun calculateSpeedInKmH() : Double? {
        previousLocation?.let { prev ->
            currentLocation?.let { now ->
                val distanceInXY = calculateComputerDistance(prev.location, now.location)
                val distanceInKm = NumberUtils.coordsDistanceToKm(distanceInXY)

                val deltaSeconds = calculateSecondsDiff(prev.timestamp, now.timestamp)
                val deltaHours = NumberUtils.secondsToHours(deltaSeconds)

                if (deltaHours == 0.0) return null
                return distanceInKm / deltaHours
            }
        }

        return null
    }

    private fun calculateComputerDistance(location1: Location, location2: Location) : Double {
        val x1 = location1.latitude
        val x2 = location2.latitude
        val y1 = location1.longitude
        val y2 = location2.longitude
        return NumberUtils.hyp(x2 - x1, y2 - y1)
    }

    private fun calculateSecondsDiff(time1: Long, time2: Long) : Long {
        val sec1 = NumberUtils.millisToSeconds(time1)
        val sec2 = NumberUtils.millisToSeconds(time2)
        return sec2 - sec1
    }
}