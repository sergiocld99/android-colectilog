package cs10.apps.travels.tracer.utils

class SafeUtils {

    companion object {

        fun hourFormat(h: Int?, m: Int?): String? {
            return if (m == null) null else "$h:${Utils.twoDecimals(m)}"
        }

        fun priceFormat(p: Double?): String? {
            return if (p == null) null else String.format("$%.2f", p)
        }
    }
}