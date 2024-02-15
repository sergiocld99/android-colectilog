package cs10.apps.travels.tracer.modules.lines.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import cs10.apps.travels.tracer.model.joins.RatedBusLine
import cs10.apps.travels.tracer.model.joins.TravelStats
import cs10.apps.travels.tracer.model.lines.CustomBusLine
import cs10.apps.travels.tracer.model.lines.HourBusStat
import cs10.apps.travels.tracer.modules.lines.entity.FrequentTravel
import cs10.apps.travels.tracer.modules.lines.model.BusDayInfo
import cs10.apps.travels.tracer.modules.lines.model.BusDestinationInfo
import cs10.apps.travels.tracer.modules.lines.model.BusRamalInfo
import cs10.apps.travels.tracer.modules.lines.model.TrainDayInfo

@Dao
interface LinesDao {

    @Insert
    fun insert(customBusLine: CustomBusLine)

    @Update
    fun update(customBusLine: CustomBusLine)

    @Query("SELECT * FROM lines")
    fun getAll() : MutableList<CustomBusLine>

    @Query("SELECT id FROM lines")
    fun getIds() : List<Int>

    @Query("SELECT DISTINCT number FROM lines")
    fun getCustomNumbers() : MutableList<Int>

    @Query("SELECT DISTINCT linea FROM Viaje WHERE linea is not null")
    fun getAllFromViajes() : List<Int>

    @Query("SELECT * FROM lines WHERE number = :number limit 1")
    fun getByNumber(number: Int) : CustomBusLine?

    @Query("SELECT nombrePdaInicio FROM Viaje WHERE linea = :number and endHour is not null " +
            "GROUP BY nombrePdaInicio ORDER BY COUNT(*) DESC LIMIT 2")
    fun getTopStopsFrom(number: Int) : List<String>

    @Query("SELECT nombrePdaInicio,nombrePdaFin FROM Viaje WHERE linea = :number and endHour is not null " +
            "GROUP BY nombrePdaInicio, nombrePdaFin ORDER BY COUNT(*) DESC LIMIT 4")
    fun getFrequentTravelsFromLine(number: Int) : List<FrequentTravel>

    @Query("SELECT linea as number, startHour as hour, AVG(rate) as averageRate " +
            "FROM viaje WHERE linea = :number and rate is not null " +
            "GROUP BY startHour ORDER BY startHour")
    fun getHourStatsForLine(number: Int) : List<HourBusStat>

    @Query("SELECT linea as number, startHour as hour, " +
            "AVG(endHour * 60 + endMinute - startHour * 60 - startMinute) as averageRate " +
            "FROM viaje WHERE linea = :number and endHour is not null " +
            "GROUP BY startHour ORDER BY startHour")
    fun getHourStatsForLineByDuration(number: Int) : List<HourBusStat>

    @Query("SELECT linea as number, startHour as hour, " +
            "AVG(endHour * 60 + endMinute - startHour * 60 - startMinute) as averageRate " +
            "FROM Viaje WHERE linea = :number and endHour is not null and nombrePdaInicio is :stop " +
            "GROUP BY startHour ORDER BY startHour")
    fun getHourStatsFromStop(number: Int, stop: String) : List<HourBusStat>

    @Query("SELECT linea as number, startHour as hour, " +
            "AVG(endHour * 60 + endMinute - startHour * 60 - startMinute) as averageRate " +
            "FROM Viaje WHERE linea = :number and endHour is not null and nombrePdaInicio is :start " +
            "and nombrePdaFin is :end GROUP BY startHour ORDER BY startHour")
    fun getHourStatsForTravel(number: Int, start: String, end: String): List<HourBusStat>

    // --------------------- JOINS -----------------------------

    @Query("SELECT L.*, AVG(V.rate) as avgUserRate, COUNT(V.rate) as reviewsCount " +
            "FROM lines L LEFT JOIN Viaje V ON L.number = V.linea " +
            "GROUP BY L.id")
    fun getAllWithRates() : MutableList<RatedBusLine>

    @Query("SELECT D.id, D.number, D.name, D.color, " +
            "AVG(D.rate) as avgUserRate, COUNT(D.rate) as reviewsCount FROM (" +
            "SELECT L.*, V.rate FROM lines L LEFT JOIN Viaje V ON L.number = V.linea " +
            "WHERE L.id = :id ORDER BY year desc, month desc, day desc limit 10) D")
    fun getStatsFrom(id: Int) : MutableList<RatedBusLine>

    @Query("SELECT DISTINCT V.ramal, L.color, AVG(V.rate) as avgUserRate, COUNT(V.rate) as reviewsCount " +
            "FROM Viaje V INNER JOIN lines L ON V.linea = L.number " +
            "WHERE V.rate is not null and V.linea is :number GROUP BY V.ramal")
    fun getRamalesFromLine(number: Int) : MutableList<BusRamalInfo>
    
    @Query("SELECT DISTINCT V.nombrePdaFin, L.color, AVG(V.rate) as avgUserRate, COUNT(V.rate) as reviewsCount " +
            "FROM Viaje V INNER JOIN lines L on V.linea = L.number " +
            "WHERE V.rate is not null and V.linea is :number GROUP BY V.nombrePdaFin")
    suspend fun getDestinationStatsForLine(number: Int) : MutableList<BusDestinationInfo>

    @Query("SELECT DISTINCT V.wd, L.color, AVG(V.rate) as avgUserRate, COUNT(V.rate) as reviewsCount " +
            "FROM Viaje V INNER JOIN lines L on V.linea = L.number " +
            "WHERE V.rate is not null and V.linea is :number GROUP BY V.wd")
    suspend fun getDayStatsForLine(number: Int) : MutableList<BusDayInfo>

    // --------------------- TRAIN ------------------------------

    @Query("SELECT DISTINCT wd, AVG(rate) as avgUserRate, COUNT(rate) as reviewsCount " +
            "FROM Viaje WHERE rate is not null and tipo = 1 GROUP BY wd")
    suspend fun getDayStatsForTrain() : MutableList<TrainDayInfo>

    @Query("SELECT P1.latitud as start_x, P1.longitud as start_y, " +
                "P2.latitud as end_x, P2.longitud as end_y, " +
                "(V.startHour * 60 + V.startMinute) as start_time," +
                "(V.endHour * 60 + V.endMinute) as end_time " +
                "FROM Viaje V INNER JOIN Parada P1 ON P1.nombre = V.nombrePdaInicio " +
                "INNER JOIN Parada P2 on P2.nombre = V.nombrePdaFin " +
                "where V.tipo = :type and V.wd = :weekDay and endHour is not null " +
                "order by year desc, month desc, day desc limit 10")
    suspend fun getRecentFinishedTravelsOn(weekDay: Int, type: Int): MutableList<TravelStats>

}