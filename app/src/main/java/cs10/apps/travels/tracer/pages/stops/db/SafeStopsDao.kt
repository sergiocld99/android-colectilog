package cs10.apps.travels.tracer.pages.stops.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import cs10.apps.travels.tracer.development.path.entity.PathGroup
import cs10.apps.travels.tracer.model.Parada
import cs10.apps.travels.tracer.pages.live.entity.MediumStop

@Dao
interface SafeStopsDao {

    @Insert
    suspend fun insertMediumStop(mediumStop: MediumStop) : Long

    @Update
    suspend fun updateMediumStop(mediumStop: MediumStop)

    @Delete
    suspend fun deleteMediumStop(mediumStop: MediumStop)

    @Query("UPDATE Parada SET nombre = :newName WHERE nombre = :oldName")
    fun renameStop(oldName: String, newName: String)

    @Query("UPDATE MediumStop SET destination = :newName WHERE destination = :oldName")
    fun renameDestinations(oldName: String, newName: String)

    @Query("UPDATE MediumStop SET prev = :newName WHERE prev = :oldName")
    fun renamePrevStops(oldName: String, newName: String)

    @Query("UPDATE MediumStop SET name = :newName WHERE name = :oldName")
    fun renameCurrentStops(oldName: String, newName: String)

    @Query("UPDATE MediumStop SET next = :newName WHERE next = :oldName")
    fun renameNextStops(oldName: String, newName: String)

    @Query("UPDATE MediumStop SET prev = :inserted " +
            "WHERE line = :line and ramal is :ramal and destination is :dest and name is :target")
    suspend fun updateNextBusMediumStop(line: Int, ramal: String?, dest: String, target: String, inserted: String)

    @Query("UPDATE MediumStop SET prev = :inserted WHERE type is :type and destination is :dest and name is :target")
    suspend fun updateNextMediumStopForType(type: Int, dest: String, target: String, inserted: String)

    @Query("UPDATE MediumStop SET next = :updatedNext WHERE id = :id")
    suspend fun updateMediumStopNextField(id: Long, updatedNext: String)

    @Query("UPDATE MediumStop SET prev = :updatedPrev WHERE id = :id")
    suspend fun updateMediumStopPrevField(id: Long, updatedPrev: String)

    @Query("SELECT * FROM MediumStop")
    suspend fun getAllMediumStops(): List<MediumStop>

    @Query("SELECT * FROM parada where nombre is :name LIMIT 1")
    fun getStopByName(name: String): Parada?

    @Query("SELECT nombre FROM parada where latitud = :latitude and longitud = :longitude")
    suspend fun getNameByCoords(latitude: Double, longitude: Double): String?

    @Query("SELECT * FROM MediumStop where line = :line and ramal is :ramal and destination is :dest")
    suspend fun getMediumStopsCreatedForBusTo(line: Int, ramal: String?, dest: String): List<MediumStop>

    @Query("SELECT * FROM MediumStop where type = :type and destination is :dest")
    suspend fun getMediumStopsCreatedForTypeTo(type: Int, dest: String): List<MediumStop>

    @Query("SELECT * FROM MediumStop where type = :type")
    suspend fun getMediumStopsCreatedForType(type: Int): List<MediumStop>

    @Query("SELECT * FROM MediumStop where prev is :target")
    suspend fun getMediumStopsJustAfter(target: String): List<MediumStop>

    @Query("SELECT * FROM MediumStop where next is :target")
    suspend fun getMediumStopsJustBefore(target: String): List<MediumStop>

    /* MEDIUM STOPS GROUP BY LINE, RAMAL, DESTINATION */
    @Query("SELECT line, ramal, destination, COUNT(*) as length FROM MediumStop where type = :type " +
            "and line is not null GROUP BY line, ramal, destination")
    suspend fun getPathGroups(type: Int): List<PathGroup>
}