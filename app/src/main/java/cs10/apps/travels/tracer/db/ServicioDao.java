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

    @Query("SELECT HT.*, cabecera, ramal FROM HorarioTren HT " +
            "inner join ServicioTren ST on ST.id = HT.service " +
            "WHERE station is :stopName " +
            "AND (HT.hour * 60 + HT.minute) >= :target " +
            "order by HT.hour, HT.minute limit :cant")
    List<RamalSchedule> getNextArrivals(String stopName, int target, int cant);

    @Query("SELECT * FROM HorarioTren WHERE service = :serviceId ORDER BY hour, minute")
    List<HorarioTren> getRecorrido(long serviceId);

    @Query("SELECT * FROM HorarioTren " +
            "WHERE service is :serviceId AND (hour * 60 + minute) BETWEEN :now and :target " +
            "ORDER BY hour, minute")
    List<HorarioTren> getRecorridoUntil(long serviceId, int now, int target);

    @Query("SELECT * FROM HorarioTren " +
            "WHERE service is :serviceId AND (hour * 60 + minute) > :target " +
            "ORDER BY hour, minute")
    List<HorarioTren> getRecorridoFrom(long serviceId, int target);

    @Query("SELECT * FROM HorarioTren WHERE service is :serviceId " +
            "ORDER BY hour desc, minute desc limit 1")
    HorarioTren getFinalStation(long serviceId);

    @Query("SELECT HT.* FROM HorarioTren HT " +
            "inner join ServicioTren ST on HT.service = ST.id " +
            "WHERE ST.ramal = :targetRamal AND HT.station = :currentStation " +
            "AND (HT.hour * 60 + HT.minute) BETWEEN :sinceTime AND (:sinceTime + :maxWait) " +
            "ORDER BY HT.hour, HT.minute limit 1")
    HorarioTren getArrival(String targetRamal, String currentStation, int sinceTime, int maxWait);

    @Query("SELECT * FROM ServicioTren " +
            "where cabecera = :station and (hora * 60 + minuto) >= :sinceTime " +
            "order by hora, minuto limit 1")
    ServicioTren getNextServiceFrom(String station, int sinceTime);

    @Query("SELECT * FROM ServicioTren where cabecera = :station order by hora, minuto limit 1")
    ServicioTren getFirstServiceFrom(String station);

    @Query("DELETE FROM HorarioTren")
    void dropHorarios();

    @Query("DELETE FROM ServicioTren")
    void dropServicios();

    @Query("DELETE FROM HorarioTren WHERE service >= :targetService")
    void deleteHorariosSince(long targetService);

    @Query("DELETE FROM ServicioTren WHERE id >= :target")
    void deleteServicesSince(long target);
}
