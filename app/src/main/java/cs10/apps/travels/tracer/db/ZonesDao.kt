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

    @Query("SELECT * FROM Zone where id = :id limit 1")
    suspend fun getZoneWithId(id: Long) : Zone?

    @Query("SELECT * FROM Zone WHERE (:x BETWEEN x0 AND x1) AND (:y BETWEEN y0 AND y1) ")
    suspend fun findZonesIn(x: Double, y:Double) : MutableList<Zone>

    @Query("SELECT * FROM Zone WHERE (:x BETWEEN x0 AND x1) AND (:y BETWEEN y0 AND y1) limit 1")
    suspend fun findFirstZoneIn(x: Double, y: Double) : Zone?

    @Query("SELECT * FROM Zone WHERE " +
            "( (x0 BETWEEN :x0 and :x1) OR (x1 BETWEEN :x0 and :x1) ) AND " +
            "( (y0 BETWEEN :y0 and :y1) OR (y1 BETWEEN :y0 and :y1) ) limit 2")
    suspend fun findPartialOverlapsIn(x0: Double, x1: Double, y0: Double, y1: Double) : MutableList<Zone>

    @Query("SELECT COUNT(*) as travelsCount, AVG(V.tipo) as averageType FROM Viaje V " +
            "INNER JOIN Parada P ON P.nombre = V.nombrePdaInicio " +
            "WHERE (P.latitud BETWEEN :x0 AND :x1) AND (P.longitud BETWEEN :y0 AND :y1) ")
    fun countTravelsIn(x0: Double, x1: Double, y0: Double, y1: Double) : ZoneStats
}