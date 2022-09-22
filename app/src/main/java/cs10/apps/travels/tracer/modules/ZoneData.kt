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

        fun getZoneUppercase(location: Location) : String? = getZone(location)?.uppercase()

        private fun getZone(location: Location) : String? {
            return getZone(getXCode(location.latitude), getYCode(location.longitude))
        }

        private fun getZone(x_code: Int, y_code: Int) : String? {

            when (x_code) {
                19 -> if (y_code == 31) return "Cruce Varela"
                20 -> if (y_code == 30) return "El Ombú"
                21 -> {
                    return when(y_code){
                        29 -> "Alpargatas"
                        28 -> "Vucetich"
                        27 -> "Villa Elisa"
                        26 -> "City Bell"
                        else -> null
                    }
                }

                22 -> {
                    return when(y_code){
                        25 -> "Hospital Gonnet"
                        24 -> "La Plata"
                        else -> null
                    }
                }
            }

            return null
        }
    }
}