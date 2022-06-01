package cs10.apps.travels.tracer.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import cs10.apps.travels.tracer.model.roca.HorarioTren;
import cs10.apps.travels.tracer.model.roca.RamalSchedule;
import cs10.apps.travels.tracer.model.roca.ServicioTren;

@Dao
public interface ServicioDao {

    @Insert
    long insert(ServicioTren servicioTren);

    @Insert
    void insertHorario(HorarioTren horarioTren);

    @Query("SELECT COUNT(*) FROM HorarioTren")
    int getHorariosCount();

    @Query("SELECT COUNT(*) FROM ServicioTren WHERE ramal is :nombre")
    int getServicesCount(String nombre);

    @Query("SELECT COUNT(*) FROM ServicioTren WHERE ramal is null")
    int getUnnamedServicesCount();

    @Query("SELECT COUNT(*) FROM ServicioTren " +
            "WHERE cabecera is :cabecera and hora is :startHour and minuto is :startM")
    int getServicesCount(String cabecera, int startHour, int startM);

    @Query("SELECT HT.*, ramal FROM HorarioTren HT " +
            "inner join ServicioTren ST on ST.id = HT.service " +
            "WHERE station is :stopName AND ((hour = :hour and minute >= :minute) or hour > :hour) " +
            "order by hour, minute limit 5")
    List<RamalSchedule> getNextArrivals(String stopName, int hour, int minute);

    @Query("SELECT * FROM HorarioTren " +
            "WHERE service is :serviceId AND ((hour = :hour and minute >= :minute) or hour > :hour)" +
            "ORDER BY hour, minute")
    List<HorarioTren> getRecorrido(long serviceId, int hour, int minute);

    @Query("SELECT * FROM HorarioTren " +
            "WHERE service is :serviceId AND (hour * 60 + minute) BETWEEN :now and :target " +
            "ORDER BY hour, minute")
    List<HorarioTren> getRecorridoUntil(long serviceId, int now, int target);

    @Query("SELECT * FROM HorarioTren WHERE service is :serviceId " +
            "ORDER BY hour desc, minute desc limit 1")
    HorarioTren getFinalStation(long serviceId);

    @Query("DELETE FROM HorarioTren")
    void dropHorarios();

    @Query("DELETE FROM ServicioTren")
    void dropServicios();
}
