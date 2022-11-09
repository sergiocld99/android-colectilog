package cs10.apps.travels.tracer.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import cs10.apps.travels.tracer.model.lines.CustomBusLine

@Dao
interface LinesDao {

    @Insert
    fun insert(customBusLine: CustomBusLine)

    @Query("SELECT * FROM lines")
    fun getAll() : MutableList<CustomBusLine>

    @Query("SELECT DISTINCT linea FROM Viaje WHERE linea is not null")
    fun getAllFromViajes() : List<Int>


}