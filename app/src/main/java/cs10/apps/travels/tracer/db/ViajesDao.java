package cs10.apps.travels.tracer.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import cs10.apps.travels.tracer.model.LineInfo;
import cs10.apps.travels.tracer.model.PriceSum;
import cs10.apps.travels.tracer.model.Viaje;
import cs10.apps.travels.tracer.model.joins.ColoredTravel;
import cs10.apps.travels.tracer.model.location.TravelDistance;

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

    @Query("SELECT V.*, L.color FROM viaje V LEFT JOIN lines L ON V.linea = L.number " +
            "ORDER BY year desc, month desc, day desc, startHour desc, startMinute desc")
    List<ColoredTravel> getAllPlusColors();

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

    @Query("SELECT costo FROM viaje " +
            "where nombrePdaInicio is :inicio and nombrePdaFin is :fin order by id desc")
    Double getLastPrice(String inicio, String fin);

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

    @Query("SELECT V.*, L.color FROM viaje V LEFT JOIN lines L ON V.linea = L.number " +
            "where nombrePdaInicio = :stopName and linea is NOT NULL " +
            "and (startHour * 60 + startMinute) >= :target " +
            "order by startHour, startMinute limit :cant")
    List<ColoredTravel> getNextArrivals(String stopName, int target, int cant);

    @Query("SELECT * FROM Viaje where wd = 0")
    List<Viaje> getUndefinedWeekDays();

    @Query("SELECT DISTINCT ramal FROM Viaje where ramal like '___%' order by ramal")
    List<String> getAllRamals();

    @Query("SELECT v.id, (p1.latitud - p2.latitud) as xDiff, (p1.longitud - p2.longitud) as yDiff " +
            "FROM Viaje v " +
            "inner join Parada p1 on v.nombrePdaInicio = p1.nombre " +
            "inner join Parada p2 on v.nombrePdaFin = p2.nombre")
    List<TravelDistance> getTravelDistances();

    @Query("SELECT * FROM Viaje where endHour is null " +
            "and (startHour * 60 + startMinute) < :currentTs order by id desc limit 1")
    Viaje getLastStartedTravel(int currentTs);

    @Query("SELECT * from viaje order by id desc limit 1")
    Viaje getLastTravel();

    @Query("SELECT MAX(id) from viaje")
    Long getLastTravelId();

    @Query("SELECT COUNT(*) from viaje where year = :year and month = :month and day = :day " +
            "and (startHour * 60 + startMinute between ((:hour - 2) * 60 + :minute) and :hour * 60 + :minute - 1)")
    int last2HoursQuantity(int year, int month, int day, int hour, int minute);

    // ------------------------------- LIVE --------------------------------------------

    // Comentario: solo se devuelve UN viaje INCOMPLETO empezado HOY, se verifica hora y minuto
    @Query("SELECT V.*, L.color FROM viaje V LEFT JOIN lines L ON V.linea = L.number " +
            "where endHour is null and year = :y and month = :m and day = :d " +
            "and (startHour*60+startMinute) < :currentTs " +
            "order by startHour desc, startMinute desc limit 1")
    ColoredTravel getCurrentTravel(int y, int m, int d, int currentTs);

    @Query("SELECT * FROM Viaje where endHour is not null and linea is not :excludedLine and " +
            "nombrePdaInicio = :startStop and nombrePdaFin is not :excludedEndStop " +
            "order by RANDOM() limit 1")
    Viaje getCompletedTravelFrom(String startStop, String excludedEndStop, Integer excludedLine);

    // ------------------------------ AUTOCOMPLETE CREATION -------------------------------------

    // Atajo: busca el viaje más probable a realizar desde la ubicación actual
    @Query("SELECT * FROM Viaje where nombrePdaInicio = :targetStart " +
            "group by linea, ramal, nombrePdaInicio, nombrePdaFin having count(*) > 2 " +
            "order by COUNT(*) desc limit 1")
    Viaje getLikelyTravelFrom(String targetStart);

    // ------------------------------ TRAVEL STATS -----------------------------------

    @Query("SELECT AVG((endHour-startHour)*60 + (endMinute-startMinute)) FROM Viaje " +
            "where linea = :line and ramal = :ramal " +
            "and nombrePdaInicio = :startStop and nombrePdaFin = :endStop")
    int getAverageTravelDuration(int line, String startStop, String endStop, String ramal);

    @Query("SELECT MAX((endHour-startHour)*60 + (endMinute-startMinute)) FROM Viaje " +
            "where linea = :line and nombrePdaInicio = :startStop and nombrePdaFin = :endStop")
    Integer getMaxTravelDuration(int line, String startStop, String endStop);

    @Query("SELECT MIN((endHour-startHour)*60 + (endMinute-startMinute)) FROM Viaje " +
            "where linea = :line and nombrePdaInicio = :startStop and nombrePdaFin = :endStop")
    Integer getMinTravelDuration(int line, String startStop, String endStop);

    @Query("SELECT MIN((endHour-startHour)*60 + (endMinute-startMinute)) FROM Viaje " +
            "where nombrePdaInicio = :startStop and nombrePdaFin = :endStop")
    Integer getTrainMinTravelDuration(String startStop, String endStop);
}
