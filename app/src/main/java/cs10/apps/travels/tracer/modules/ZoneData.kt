package cs10.apps.travels.tracer.modules

import android.location.Location

class ZoneData {

    companion object {

        private fun getXCode(latitude: Double) : Int {
            val normalized = (-latitude - 34) * 100
            return normalized.toInt() / 4
        }

        private fun getYCode(longitude: Double) : Int {
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
                        else -> null
                    }
                }
                21 -> {
                    return when(y_code){
                        29 -> "Alpargatas"
                        28 -> "Pereyra"
                        27 -> "Villa Elisa"
                        26 -> "City Bell"
                        else -> null
                    }
                }

                22 -> {
                    return when(y_code){
                        25 -> "Gonnet"
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