package cs10.apps.travels.tracer.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import cs10.apps.travels.tracer.model.LineInfo;
import cs10.apps.travels.tracer.model.Viaje;

@Dao
public interface ViajesDao {

    @Insert
    void insert(Viaje viaje);

    @Update
    void update(Viaje viaje);

    @Query("DELETE FROM viaje where id is :id")
    void delete(long id);

    @Query("SELECT * FROM viaje ORDER BY year desc, month desc, day desc, startHour desc, startMinute desc")
    List<Viaje> getAll();

    @Query("SELECT * FROM viaje where id is :travelId LIMIT 1")
    Viaje getById(long travelId);

    @Query("SELECT linea, count(linea) as count FROM viaje group by linea order by count limit 5")
    List<LineInfo> getFavouriteLines();
}
