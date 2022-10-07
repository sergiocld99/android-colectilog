package cs10.apps.travels.tracer.modules

import android.location.Location

class ZoneData {

    companion object {

        fun getCodes(location: Location) : Pair<Int, Int> {
            return Pair(getXCode(location.latitude), getYCode(location.longitude))
        }

        fun getXCode(latitude: Double) : Int {
            val normalized = (-latitude - 34) * 100
            return normalized.toInt() / 4
        }

        fun getYCode(longitude: Double) : Int {
            val normalized = (-longitude - 57) * 100
            return normalized.toInt() / 4
        }

        fun getZoneUppercase(location: Location) : String = getZone(location).uppercase()

        private fun getZone(location: Location) : String {
            val xCode = getXCode(location.latitude)
            val yCode = getYCode(location.longitude)

            return getZone(xCode, yCode) ?: "Zona ($xCode; $yCode)"
        }

        private fun getZone(x_code: Int, y_code: Int) : String? {

            when (x_code) {
                19 -> if (y_code == 31) return "Cruce Varela"
                20 -> {
                    return when(y_code){
                        32 -> "Arco de San Jorge"
                        31 -> "Varela"
                        30 -> "Bosques"
                        29 -> "Gutiérrez"
                        else -> null
                    }
                }
                21 -> {
                    return when(y_code){
                        30 -> "Ingeniero Allan"          // -58.20
                        29 -> "Alpargatas"          // -58.16
                        28 -> "Pereyra"                 // -58.12
                        27 -> "Villa Elisa"                 // -58.08
                        26 -> "Transradio / City Bell"   
                        25 -> "Batallón 601"
                        else -> null
                    }
                }
                22 -> {
                    return when(y_code){
                        26 -> "City Bell (467 - 476)"       // -58.04
                        25 -> "Gonnet"                  // -58.00
                        24 -> "La Plata"
                        23 -> "Facultades"
                        else -> null
                    }
                }
            }

            return null
        }
    }
}