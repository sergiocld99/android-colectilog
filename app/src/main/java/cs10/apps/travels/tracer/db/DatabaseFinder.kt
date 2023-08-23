package cs10.apps.travels.tracer.db

import cs10.apps.travels.tracer.enums.TransportType
import cs10.apps.travels.tracer.model.Viaje
import java.util.Calendar

class DatabaseFinder(val db: MiDB) {


    fun countTravelsLast2Hours(exceptCar: Boolean): Int {
        val calendar = Calendar.getInstance()
        val day = calendar[Calendar.DAY_OF_MONTH]
        val month = calendar[Calendar.MONTH] + 1
        val year = calendar[Calendar.YEAR]
        val hour = calendar[Calendar.HOUR_OF_DAY]
        val minute = calendar[Calendar.MINUTE]
        val start = standardTime(hour-2, minute)
        val end = standardTime(hour, minute-2)
        val exceptedType = if (exceptCar) TransportType.CAR.ordinal else -1

        return db.viajesDao().countTravelsInTimeRange(year, month, day, start, end, exceptedType)
    }

    fun countTravels2HoursBefore(viaje: Viaje, exceptCar: Boolean): Int {
        val day = viaje.day
        val month = viaje.month
        val year = viaje.year
        val start = standardTime(viaje.startHour-2, viaje.startMinute)
        val end = standardTime(viaje.startHour, viaje.startMinute-2)
        val exceptedType = if (exceptCar) TransportType.CAR.ordinal else -1

        return db.viajesDao().countTravelsInTimeRange(year, month, day, start, end, exceptedType)
    }

    private fun standardTime(hour: Int, minute: Int) = hour * 60 + minute

}