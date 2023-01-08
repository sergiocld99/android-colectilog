package cs10.apps.common.android

import java.util.concurrent.TimeUnit
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

class NumberUtils {

    companion object {

        fun coordsDistanceToKm(distance: Double) : Double = distance * 91.97
        fun kmToCoordsDistance(value: Double): Double = value / 91.97

        fun hyp(x: Double, y: Double) : Double = sqrt(x.pow(2) + y.pow(2))

        fun millisToSeconds(millis: Long) : Long = TimeUnit.MILLISECONDS.toSeconds(millis)
        fun secondsToHours(seconds: Long) : Double = seconds / 3600.0
        fun minutesToHours(minutes: Int) : Double = minutes / 60.0

        fun round(valueToRound: Double, precision: Int) : Int {
            return (valueToRound / precision).roundToInt() * precision
        }

        fun round(valueToRound: Double, precision: Double) : Double {
            return (valueToRound / precision).roundToInt() * precision
        }
    }
}