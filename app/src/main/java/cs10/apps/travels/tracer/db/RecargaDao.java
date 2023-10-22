package cs10.apps.travels.tracer.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import cs10.apps.travels.tracer.model.Recarga;

@Dao
public interface RecargaDao {

    @Insert
    void insert(Recarga recarga);

    @Query("SELECT SUM(mount) FROM Recarga WHERE id > :id")
    Double getTotalChargedSince(long id);

    @Query("SELECT * FROM Recarga ORDER BY id DESC LIMIT 1")
    Recarga getLastInserted();

    @Query("SELECT MAX(id) FROM Recarga")
    Long getLastId();
}
