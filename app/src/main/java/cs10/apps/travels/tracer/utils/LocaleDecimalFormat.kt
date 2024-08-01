package cs10.apps.travels.tracer.utils

class LocaleDecimalFormat(private val decimalCount: Int) {

    fun format(doubleValue: Double): String {
        return String.format("%.${decimalCount}f", doubleValue)
    }
}