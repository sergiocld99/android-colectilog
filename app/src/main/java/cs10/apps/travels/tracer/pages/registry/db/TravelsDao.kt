package cs10.apps.travels.tracer.pages.registry.db

import androidx.room.Dao
import androidx.room.Query
import cs10.apps.travels.tracer.model.Viaje
import cs10.apps.travels.tracer.model.joins.PriceSum
import cs10.apps.travels.tracer.model.joins.TravelStats
import cs10.apps.travels.tracer.pages.month_summary.model.TimeLineStat

/** ViajesDao implementado en Kotlin
 *
 */

@Dao
interface TravelsDao {

    @Query("UPDATE Viaje SET nombrePdaInicio = :newName WHERE nombrePdaInicio = :oldName")
    suspend fun renameStartPlaces(oldName: String, newName: String)

    @Query("UPDATE Viaje SET nombrePdaFin = :newName WHERE nombrePdaFin = :oldName")
    suspend fun renameEndPlaces(oldName: String, newName: String)

    @Query("SELECT SUM(costo) FROM viaje where id > :travelId")
    suspend fun getTotalSpentSince(travelId: Long) : Double?

    @Query("SELECT SUM(costo) FROM viaje where tipo = :type and id > :travelId")
    suspend fun getTotalSpentInTypeSince(travelId: Long, type: Int) : Double?

    @Query("SELECT SUM(costo) FROM viaje where tipo = :type and year = :year and month = :month")
    suspend fun getTotalSpentInMonthInType(month: Int, year: Int, type: Int) : Double?

    @Query("SELECT V.linea, L.color, SUM(V.costo) as suma FROM viaje V " +
            "LEFT JOIN lines L ON V.linea = L.number " +
            "where linea is not null and month is :month and year is :year " +
            " group by linea order by 3 desc limit 3")
    suspend fun getMostSpentBusLineInMonth(month: Int, year: Int): List<PriceSum>

    @Query("SELECT COUNT(rate) FROM Viaje where tipo = :type")
    fun getReviewsCountForType(type: Int): Int

    @Query("SELECT AVG(rate) FROM viaje where tipo = :type")
    fun getAverageRateForType(type: Int): Double?

    @Query("SELECT COUNT(rate) FROM Viaje WHERE tipo = :type AND year = :year AND month >= :startMonth")
    suspend fun getReviewsCountForTypeSince(type: Int, year: Int, startMonth: Int): Int

    @Query("SELECT AVG(rate) FROM Viaje WHERE tipo = :type AND year = :year AND month >= :startMonth")
    suspend fun getAverageRateForTypeSince(type: Int, year: Int, startMonth: Int): Double?

    // ------------------------------ LINE STATS -------------------------------
    @Query(
        "SELECT P1.latitud as start_x, P1.longitud as start_y, " +
                "P2.latitud as end_x, P2.longitud as end_y, " +
                "(V.startHour * 60 + V.startMinute) as start_time," +
                "(V.endHour * 60 + V.endMinute) as end_time " +
                "FROM Viaje V " +
                "INNER JOIN Parada P1 ON P1.nombre = V.nombrePdaInicio " +
                "INNER JOIN Parada P2 on P2.nombre = V.nombrePdaFin " +
                "where V.tipo = :type and endHour is not null " +
                "order by year desc, month desc, day desc limit 10"
    )
    suspend fun getRecentFinishedTravelsFromType(type: Int): List<TravelStats>

    @Query("SELECT SUM(endHour * 60 + endMinute - startHour * 60 - startMinute) as timeSpent, " +
            "linea as lineNumber FROM viaje " +
            "where year = :year and month = :month and endHour not null and lineNumber > 0 " +
            "GROUP BY lineNumber ORDER BY timeSpent DESC LIMIT 5")
    suspend fun getTimeSpentInMonth(month: Int, year: Int): List<TimeLineStat>

    @Query("SELECT V2.* FROM Viaje V1 INNER JOIN Viaje V2 " +
            "ON V1.endHour = V2.startHour AND V1.nombrePdaFin = V2.nombrePdaInicio " +
            "AND V1.day = V2.day AND V1.month = V2.month AND V1.year = V2.year " +
            "WHERE V1.linea = :line AND V1.nombrePdaFin = :stop AND V1.endHour = :hour " +
            "ORDER BY V2.year DESC, V2.month DESC, V2.day DESC")
    suspend fun getBusCombinations(line: Int, stop: String, hour: Int): List<Viaje>

    @Query("SELECT AVG(V2.startMinute - V1.endMinute) FROM Viaje V1 INNER JOIN Viaje V2 " +
            "ON V1.endHour = V2.startHour AND V1.nombrePdaFin = V2.nombrePdaInicio " +
            "AND V1.day = V2.day AND V1.month = V2.month AND V1.year = V2.year " +
            "WHERE V1.linea = :line1 AND V1.nombrePdaFin = :stop " +
            "AND V2.linea = :line2 AND V2.nombrePdaInicio = :stop")
    suspend fun getBusCombinationWaiting(line1: Int, line2: Int, stop: String): Double

    @Query("SELECT * FROM Viaje WHERE day = :day AND month = :month AND year = :year AND linea = :line")
    suspend fun getBusStartedTravels(day: Int, month: Int, year: Int, line: Int) : List<Viaje>
}
