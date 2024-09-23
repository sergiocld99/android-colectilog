package cs10.apps.travels.tracer.common.enums

import android.content.Context
import cs10.apps.travels.tracer.R

enum class PrimaryDirection {
    NORTH, WEST, EAST, SOUTH;

    fun legibleString(context: Context): String {
        return when(this){
            NORTH -> context.getString(R.string.north)
            WEST -> context.getString(R.string.west)
            EAST -> context.getString(R.string.east)
            SOUTH -> context.getString(R.string.south)
        }
    }
}