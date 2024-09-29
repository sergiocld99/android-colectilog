package cs10.apps.travels.tracer.pages.registry.db

import androidx.room.Dao
import androidx.room.Query
import cs10.apps.travels.tracer.model.joins.PriceSum
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
    fun getAverageRateForType(type: Int): Double

    @Query("SELECT SUM(endHour * 60 + endMinute - startHour * 60 - startMinute) as timeSpent, " +
            "linea as lineNumber FROM viaje " +
            "where year = :year and month = :month and endHour not null and lineNumber > 0 " +
            "GROUP BY lineNumber ORDER BY timeSpent DESC LIMIT 5")
    suspend fun getTimeSpentInMonth(month: Int, year: Int): List<TimeLineStat>
}