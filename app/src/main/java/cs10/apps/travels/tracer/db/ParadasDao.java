package cs10.apps.travels.tracer.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import cs10.apps.travels.tracer.model.Parada;

@Dao
public interface ParadasDao {

    @Query("SELECT * FROM parada where nombre is :stopName")
    Parada getByName(String stopName);

    @Insert
    void insert(Parada parada);

    @Update
    void update(Parada parada);

    @Query("DELETE FROM parada where nombre is :stopName")
    void delete(String stopName);

    @Query("SELECT * FROM parada")
    List<Parada> getAll();
}
