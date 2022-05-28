package cs10.apps.travels.tracer.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

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

    @Query("SELECT * FROM (select p.*, linea, ramal, startHour, startMinute, nombrePdaFin " +
            "from parada p, viaje v " +
            "where p.nombre is v.nombrePdaInicio " +
            "and ((startHour is :hour and startMinute >= :minute) or startHour > :hour) " +
            "order by startHour, startMinute) group by nombre order by startHour, startMinute")
    List<ScheduledParada> getScheduledStopsFrom(int hour, int minute);

    @Query("SELECT * FROM (select p.*, linea, ramal, startHour, startMinute, nombrePdaInicio " +
            "from parada p, viaje v " +
            "where p.nombre is v.nombrePdaFin " +
            "and ((startHour is :hour and startMinute >= :minute) or startHour > :hour) " +
            "order by startHour, startMinute) group by nombre order by startHour, startMinute")
    List<ScheduledParada> getScheduledStopsTo(int hour, int minute);

    @Query("SELECT * FROM parada p " +
            "order by (latitud - :x) * (latitud - :x) + (longitud - :y) * (longitud - :y) limit 1")
    Parada getClosestParada(double x, double y);
}
