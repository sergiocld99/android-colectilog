package cs10.apps.travels.tracer.common.enums

import android.content.Context
import cs10.apps.travels.tracer.R

enum class TransportType {
    BUS, TRAIN, CAR, METRO;

    fun getColor(): Int {
        return when(this){
            TRAIN -> R.color.train
            CAR -> R.color.bus_159
            METRO -> R.color.bus_148
            BUS -> R.color.bus
        }
    }

    companion object {
        fun fromOrdinal(ordinal: Int): TransportType {
            return when(ordinal) {
                0 -> BUS
                1 -> TRAIN
                2 -> CAR
                3 -> METRO
                else -> BUS
            }
        }

        fun getTypesStr(context: Context) = arrayOf(
            context.getString(R.string.bus),
            context.getString(R.string.train),
            context.getString(R.string.car),
            context.getString(R.string.metro)
        )
    }
}