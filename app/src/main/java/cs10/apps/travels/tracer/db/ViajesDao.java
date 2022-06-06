package cs10.apps.travels.tracer.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import cs10.apps.travels.tracer.model.LineInfo;
import cs10.apps.travels.tracer.model.PriceSum;
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

    @Query("SELECT linea, count(linea) as count FROM viaje group by linea order by 2 limit 5")
    List<LineInfo> getFavouriteLines();

    @Query("SELECT SUM(costo) FROM viaje where linea is not null")
    double getTotalSpentInBuses();

    @Query("SELECT SUM(costo) FROM viaje where linea is not null and month is :month")
    double getTotalSpentInBuses(int month);

    @Query("SELECT SUM(costo) FROM viaje where linea is null")
    double getTotalSpentInTrains();

    @Query("SELECT SUM(costo) FROM viaje where linea is null and month is :month")
    double getTotalSpentInTrains(int month);

    @Query("SELECT MAX(costo) FROM viaje where nombrePdaInicio is :inicio and nombrePdaFin is :fin")
    Double getMaxPrice(String inicio, String fin);

    @Query("SELECT linea, SUM(costo) as suma FROM viaje " +
            "where linea is not null and month is :month group by linea order by 2 desc limit 3")
    List<PriceSum> getMostExpensiveBus(int month);

    @Query("SELECT MAX(id) FROM viaje")
    long getLastId();

    @Query("SELECT SUM(costo) FROM viaje where linea is not null and id > :travelId")
    double getSpentInBusesSince(long travelId);

    @Query("SELECT SUM(costo) FROM viaje where linea is null and id > :travelId")
    double getSpentInTrainsSince(long travelId);

    @Query("SELECT DISTINCT linea FROM viaje WHERE linea IS NOT NULL " +
            "GROUP BY linea ORDER BY COUNT(id) DESC")
    List<Integer> getAllBuses();

    @Query("SELECT * FROM Viaje where nombrePdaInicio = :stopName and linea is NOT NULL " +
            "and (startHour * 60 + startMinute) between :target and (:target + :timelapse) " +
            "order by startHour, startMinute limit :cant")
    List<Viaje> getNextArrivals(String stopName, int target, int cant, int timelapse);
}
