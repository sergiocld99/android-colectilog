package cs10.apps.travels.tracer.db;

import androidx.annotation.Nullable;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import cs10.apps.travels.tracer.model.Parada;
import cs10.apps.travels.tracer.model.joins.ScheduledParada;

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

    @Query("SELECT * FROM (select p.*, linea, ramal, L.color, startHour, startMinute, nombrePdaFin " +
            "from parada p inner join viaje v on v.nombrePdaInicio = p.nombre " +
            "LEFT JOIN lines L ON v.linea = L.number " +
            "where (startHour is :hour and startMinute >= :minute) or startHour > :hour " +
            "order by startHour, startMinute) group by nombre order by startHour, startMinute")
    List<ScheduledParada> getScheduledStopsFrom(int hour, int minute);

    @Query("SELECT * FROM (select p.*, linea, ramal, L.color, startHour, startMinute, nombrePdaInicio " +
            "from parada p inner join viaje v on v.nombrePdaFin = p.nombre " +
            "LEFT JOIN lines L ON v.linea = L.number " +
            "where (startHour is :hour and startMinute >= :minute) or startHour > :hour " +
            "order by startHour, startMinute) group by nombre order by startHour, startMinute")
    List<ScheduledParada> getScheduledStopsTo(int hour, int minute);

    @Query("SELECT p.* FROM parada p " +
            "inner join viaje v on v.nombrePdaInicio = p.nombre " +
            "group by nombre order by count(*) desc limit 8")
    List<Parada> getGeneralFavouriteStops();

    @Query("SELECT p.* FROM parada p " +
            "inner join viaje v on v.nombrePdaInicio = p.nombre where v.wd = :current " +
            "group by nombre order by count(*) desc limit 8")
    List<Parada> getFavouriteStops(int current);

    @Query("SELECT COUNT(*) + 1 " +
            "FROM (select * from viaje group by nombrePdaInicio having count(*) > :travelsInCurrent)")
    int getRank(int travelsInCurrent);

    @Query("SELECT COUNT(*) FROM viaje where nombrePdaInicio = :stopName")
    int getTravelCount(String stopName);

    @Query("SELECT * FROM Parada WHERE nombre IN " +
            "(SELECT nombrePdaInicio FROM Viaje WHERE linea IS :busLine) OR nombre IN" +
            "(SELECT nombrePdaFin FROM Viaje WHERE linea is :busLine)")
    List<Parada> getParadasWhereStops(int busLine);

    @Query("SELECT * FROM parada where tipo = 1")
    List<Parada> getCustomTrainStops();

    // ============================= TRAVEL CREATION ===========================

    @Query("SELECT P.* FROM Parada P LEFT JOIN Viaje V ON P.nombre = V.nombrePdaInicio " +
            "GROUP BY P.nombre ORDER BY COUNT(V.id) desc ")
    List<Parada> getAllOrderedByTravelCount();


    // ============================== LIVE WAITING ==========================

    @Nullable
    @Query("SELECT * FROM parada P where (P.latitud between :x0 and :x1) " +
            "and (P.longitud between :y0 and :y1) limit 1")
    Parada findStopIn(double x0, double x1, double y0, double y1);
}
