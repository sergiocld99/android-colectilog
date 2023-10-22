package cs10.apps.travels.tracer.db

import cs10.apps.common.android.NumberUtils
import cs10.apps.travels.tracer.enums.TransportType
import cs10.apps.travels.tracer.model.Viaje
import cs10.apps.travels.tracer.model.joins.TravelStats
import cs10.apps.travels.tracer.modules.live.model.EstimationData
import java.util.Calendar
import kotlin.math.roundToInt

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

    fun findMinimalDuration(t: Viaje) : Int? {
        t.linea?.let {
            val min: Int? = db.viajesDao().getMinTravelDuration(it, t.nombrePdaInicio, t.nombrePdaFin)
            if (min != null && min > 0) return min
        }

        val min = db.viajesDao().getTrainMinTravelDuration(t.nombrePdaInicio, t.nombrePdaFin)
        if (min != null && min > 0) return min

        return null
    }

    fun findAverageDuration(t: Viaje) : EstimationData? {
        val td = db.viajesDao().getTravelDistanceFromId(t.id)
        val duration = findAverageDuration(td.distance, t)

        duration?.let {
            val speed = td.distance / NumberUtils.minutesToHours(duration)
            return EstimationData(duration, speed.roundToInt(), true)
        }

        return null
    }

    /**
     * Returns duration in minutes
     */
    private fun findAverageDuration(distance: Double, t: Viaje): Int? {
        val from = t.nombrePdaInicio
        val to = t.nombrePdaFin
        val line = t.linea
        val hour = t.startHour

        // segun promedio del ramal para dicho origen-destino en la franja horaria
        t.ramal?.let {
            val expected = db.viajesDao().getAverageTravelDurationWithRamal(from, to, it, hour)
            if (expected > 0) return expected
        }

        // segun promedio general para dicho origen-destino en la franja horaria
        val expected = db.viajesDao().getAverageTravelDuration(from, to, hour)
        if (expected > 0) return expected

        // según velocidad del ultimo viaje de dicha linea
        val pastStats: TravelStats? = if (line != null) db.viajesDao().getLastFinishedTravelFromLine(line)
        else db.viajesDao().lastFinishedTrainTravel

        pastStats?.calculateSpeedInKmH()?.let { speed ->
            return (distance / speed).times(60).roundToInt()
        }

        return null
    }
}