package cs10.apps.travels.tracer.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import cs10.apps.travels.tracer.model.joins.BusRamalInfo
import cs10.apps.travels.tracer.model.joins.RatedBusLine
import cs10.apps.travels.tracer.model.lines.CustomBusLine
import cs10.apps.travels.tracer.model.lines.HourBusStat

@Dao
interface LinesDao {

    @Insert
    fun insert(customBusLine: CustomBusLine)

    @Update
    fun update(customBusLine: CustomBusLine)

    @Query("SELECT * FROM lines")
    fun getAll() : MutableList<CustomBusLine>

    @Query("SELECT DISTINCT number FROM lines")
    fun getCustomNumbers() : MutableList<Int>

    @Query("SELECT DISTINCT linea FROM Viaje WHERE linea is not null")
    fun getAllFromViajes() : List<Int>

    @Query("SELECT * FROM lines WHERE number = :number limit 1")
    fun getByNumber(number: Int) : CustomBusLine?

    @Query("SELECT linea as number, startHour as hour, AVG(rate) as averageRate " +
            "FROM viaje WHERE linea = :number and rate is not null " +
            "GROUP BY startHour ORDER BY startHour")
    fun getHourStatsForLine(number: Int) : List<HourBusStat>

    // --------------------- JOINS -----------------------------

    @Query("SELECT L.*, AVG(V.rate) as avgUserRate, COUNT(V.rate) as reviewsCount " +
            "FROM lines L LEFT JOIN Viaje V ON L.number = V.linea " +
            "GROUP BY L.id")
    fun getAllWithRates() : MutableList<RatedBusLine>

    @Query("SELECT DISTINCT V.ramal, L.color, AVG(V.rate) as avgUserRate, COUNT(V.rate) as reviewsCount " +
            "FROM Viaje V INNER JOIN lines L ON V.linea = L.number " +
            "WHERE V.rate is not null and V.linea is :number GROUP BY V.ramal")
    fun getRamalesFromLine(number: Int) : MutableList<BusRamalInfo>
}