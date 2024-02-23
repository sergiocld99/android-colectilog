package cs10.apps.travels.tracer.modules.stops.db

import androidx.room.Dao
import androidx.room.Query
import cs10.apps.travels.tracer.model.roca.RamalSchedule

@Dao
interface TrainsDao {

    @Query("SELECT DISTINCT hour FROM HorarioTren WHERE station is :station order by hour")
    suspend fun getAvailableHours(station: String) : List<Int>

    @Query("SELECT HT.*, cabecera, ramal FROM HorarioTren HT " +
            "inner join ServicioTren ST on ST.id = HT.service " +
            "WHERE station is :fromStation " +
            "order by HT.hour, HT.minute")
    suspend fun findAllArrivals(fromStation: String) : List<RamalSchedule>

    @Query("SELECT HT.*, cabecera, ramal FROM HorarioTren HT INNER JOIN ServicioTren ST ON ST.id = HT.service " +
            "WHERE station is :fromStation and hour = :hour order by HT.minute")
    suspend fun findArrivalsAt(fromStation: String, hour: Int) : List<RamalSchedule>
}