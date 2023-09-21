package cs10.apps.travels.tracer.utils

import cs10.apps.travels.tracer.R
import cs10.apps.travels.tracer.Utils

class ColorUtils {

    companion object {

        fun colorFor(busNumber: Int?, type: Int): Int {
            busNumber?.let { return getHardcodedColorFor(it) }
            return Utils.colorForType(type)
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