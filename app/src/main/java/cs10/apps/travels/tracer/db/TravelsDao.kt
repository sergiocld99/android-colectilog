package cs10.apps.travels.tracer.db

import androidx.room.Dao
import androidx.room.Query
import cs10.apps.travels.tracer.model.joins.PriceSum

/** ViajesDao implementado en Kotlin
 *
 */

@Dao
interface TravelsDao {

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
}