package cs10.apps.travels.tracer.utils

import cs10.apps.travels.tracer.R
import cs10.apps.travels.tracer.common.enums.TransportType

class ColorUtils {

    companion object {

        fun colorFor(busNumber: Int?, type: Int, initialStopName: String): Int {
            busNumber?.let { return getHardcodedColorFor(it) }
            if (type == TransportType.METRO.ordinal) return metroColorFor(initialStopName)
            return Utils.colorForType(type)
        }

        fun metroColorFor(stopName: String): Int {
            return if (stopName.endsWith("(A)")) R.color.bus
            else if (stopName.endsWith("(B)")) R.color.bus_414
            else if (stopName.endsWith("(C)")) R.color.train
            else if (stopName.endsWith("(D)")) R.color.bus_500
            else if (stopName.endsWith("(E)")) R.color.purple_500
            else if (stopName.endsWith("(H)")) R.color.bus_148
            else R.color.black
        }

        private fun getHardcodedColorFor(busNumber: Int): Int {
            return when(busNumber){
                202 -> R.color.bus_202
                324 -> R.color.bus_324
                160 -> R.color.bus_414
                178 -> R.color.bus_414
                414 -> R.color.bus_414
                159 -> R.color.bus_159
                603 -> R.color.bus_159
                383 -> R.color.bus_500
                500 -> R.color.bus_500
                508 -> R.color.bus_500
                98 -> R.color.bus_98
                148 -> R.color.bus_98
                else -> R.color.bus
            }
        }
    }
}