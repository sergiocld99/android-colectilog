package cs10.apps.travels.tracer.db

import androidx.room.*
import cs10.apps.travels.tracer.model.Zone
import cs10.apps.travels.tracer.model.joins.ZoneStats

@Dao
interface ZonesDao {

    @Insert
    fun insert(zone: Zone)

    @Update
    fun update(zone: Zone)

    @Delete
    fun delete(zone: Zone)

    @Query("SELECT * FROM Zone")
    suspend fun getAll() : MutableList<Zone>

    @Query("SELECT * FROM Zone WHERE (:x BETWEEN x0 AND x1) AND (:y BETWEEN y0 AND y1) ")
    fun findZonesIn(x: Double, y:Double) : List<Zone>

    @Query("SELECT COUNT(*) as travelsCount, AVG(V.tipo) as averageType FROM Viaje V " +
            "INNER JOIN Parada P ON P.nombre = V.nombrePdaInicio " +
            "WHERE (P.latitud BETWEEN :x0 AND :x1) AND (P.longitud BETWEEN :y0 AND :y1) ")
    fun countTravelsIn(x0: Double, x1: Double, y0: Double, y1: Double) : ZoneStats
}