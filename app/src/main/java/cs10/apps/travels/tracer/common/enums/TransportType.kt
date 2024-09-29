package cs10.apps.travels.tracer.common.enums

import android.content.Context
import cs10.apps.travels.tracer.R

enum class TransportType {
    BUS, TRAIN, CAR, METRO;

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