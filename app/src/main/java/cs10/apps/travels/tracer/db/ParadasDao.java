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
            "from parada p inner join viaje v on v.nombrePdaInicio = p.nombre " +
            "where (startHour is :hour and startMinute >= :minute) or startHour > :hour " +
            "order by startHour, startMinute) group by nombre order by startHour, startMinute")
    List<ScheduledParada> getScheduledStopsFrom(int hour, int minute);

    @Query("SELECT * FROM (select p.*, linea, ramal, startHour, startMinute, nombrePdaInicio " +
            "from parada p inner join viaje v on v.nombrePdaFin = p.nombre " +
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
}
