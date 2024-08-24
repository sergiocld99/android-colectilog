package cs10.apps.travels.tracer.utils

import java.util.Locale

class LocaleDecimalFormat(private val decimalCount: Int) {

    fun format(doubleValue: Double): String {
        return String.format(Locale.ENGLISH, "%.${decimalCount}f", doubleValue)
    }
}