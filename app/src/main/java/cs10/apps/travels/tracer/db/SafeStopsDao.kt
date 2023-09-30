package cs10.apps.travels.tracer.db

import androidx.room.Dao
import androidx.room.Query
import cs10.apps.travels.tracer.model.Parada

@Dao
interface SafeStopsDao {

    @Query("SELECT * FROM parada where nombre is :name LIMIT 1")
    fun getStopByName(name: String): Parada?
}