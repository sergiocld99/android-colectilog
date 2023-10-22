package cs10.apps.travels.tracer.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import cs10.apps.travels.tracer.model.Parada
import cs10.apps.travels.tracer.modules.live.entity.MediumStop

@Dao
interface SafeStopsDao {

    @Insert
    suspend fun insertMediumStop(mediumStop: MediumStop) : Long

    @Delete
    suspend fun deleteMediumStop(mediumStop: MediumStop)

    @Query("UPDATE MediumStop SET prev = :inserted " +
            "WHERE line = :line and ramal is :ramal and destination is :dest and name is :target")
    suspend fun updateNextBusMediumStop(line: Int, ramal: String?, dest: String, target: String, inserted: String)

    @Query("SELECT * FROM parada where nombre is :name LIMIT 1")
    fun getStopByName(name: String): Parada?

    @Query("SELECT nombre FROM parada where latitud = :latitude and longitud = :longitude")
    suspend fun getNameByCoords(latitude: Double, longitude: Double): String?

    @Query("SELECT * FROM MediumStop where line = :line and ramal is :ramal and destination is :dest")
    suspend fun getMediumStopsCreatedForBusTo(line: Int, ramal: String?, dest: String): List<MediumStop>

    @Query("SELECT * FROM MediumStop where type = :type")
    suspend fun getMediumStopsCreatedForType(type: Int): List<MediumStop>

    @Query("SELECT * FROM MediumStop where prev is :target")
    suspend fun getMediumStopsJustAfter(target: String): List<MediumStop>

    @Query("SELECT * FROM MediumStop where next is :target")
    suspend fun getMediumStopsJustBefore(target: String): List<MediumStop>
}