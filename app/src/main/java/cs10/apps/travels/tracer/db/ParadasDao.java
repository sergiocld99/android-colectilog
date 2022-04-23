package cs10.apps.travels.tracer.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import cs10.apps.travels.tracer.model.CountedParada;
import cs10.apps.travels.tracer.model.Parada;
import cs10.apps.travels.tracer.model.ScheduledParada;

@Dao
public interface ParadasDao {

    @Query("SELECT * FROM parada where nombre is :stopName LIMIT 1")
    Parada getByName(String stopName);

    @Insert
    void insert(Parada parada);

    @Update
    void update(Parada parada);

    @Query("DELETE FROM parada where nombre is :stopName")
    void delete(String stopName);

    @Query("SELECT * FROM parada ORDER BY nombre")
    List<Parada> getAll();

    @Query("SELECT p.*, (select count(*) from viaje v where p.nombre is v.nombrePdaInicio) as veces " +
            "FROM parada p order by veces desc, nombre")
    List<CountedParada> getCountedStops();

    @Query("SELECT * FROM (select p.*, linea, startHour, startMinute from parada p, viaje v " +
            "where p.nombre is v.nombrePdaInicio " +
            "and ((startHour is :hour and startMinute >= :minute) or startHour > :hour) " +
            "order by startHour, startMinute) group by nombre order by startHour, startMinute")
    List<ScheduledParada> getScheduledStops(int hour, int minute);
}
