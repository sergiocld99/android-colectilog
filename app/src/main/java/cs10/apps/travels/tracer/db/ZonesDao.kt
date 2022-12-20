package cs10.apps.travels.tracer.db

import androidx.room.*
import cs10.apps.travels.tracer.model.Zone

@Dao
interface ZonesDao {

    @Insert
    fun insert(zone: Zone)

    @Update
    fun update(zone: Zone)

    @Delete
    fun delete(zone: Zone)

    @Query("SELECT * FROM Zone")
    fun getAll() : MutableList<Zone>
}