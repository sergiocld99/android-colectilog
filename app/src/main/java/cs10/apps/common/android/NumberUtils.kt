package cs10.apps.common.android

import java.util.concurrent.TimeUnit
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

class NumberUtils {

    companion object {

        fun coordsDistanceToKm(distance: Double) : Double {
            return distance * 91.97
        }

        fun hyp(x: Double, y: Double) : Double {
            return sqrt(x.pow(2) + y.pow(2))
        }

        fun millisToSeconds(millis: Long) : Long {
            return TimeUnit.MILLISECONDS.toSeconds(millis)
        }

        fun secondsToHours(seconds: Long) : Double {
            return seconds / 3600.0
        }

        fun roundWithPresicion(valueToRound: Double, precision: Int) : Int {
            return (valueToRound / precision).roundToInt() * precision
        }
    }
}