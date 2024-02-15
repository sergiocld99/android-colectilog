package cs10.apps.travels.tracer.modules.stops.db

import androidx.room.Dao
import androidx.room.Query
import cs10.apps.travels.tracer.model.roca.RamalSchedule

@Dao
interface TrainsDao {

    @Query("SELECT HT.*, cabecera, ramal FROM HorarioTren HT " +
            "inner join ServicioTren ST on ST.id = HT.service " +
            "WHERE station is :fromStation " +
            "order by HT.hour, HT.minute")
    suspend fun findAllArrivals(fromStation: String) : List<RamalSchedule>
}