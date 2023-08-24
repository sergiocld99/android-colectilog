package cs10.apps.travels.tracer.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import cs10.apps.travels.tracer.model.Viaje;
import cs10.apps.travels.tracer.model.joins.ColoredTravel;
import cs10.apps.travels.tracer.model.joins.PriceSum;
import cs10.apps.travels.tracer.model.joins.TravelStats;
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

    @Query("SELECT V.*, L.color FROM viaje V LEFT JOIN lines L ON V.linea = L.number " +
            "WHERE V.linea = :line and V.wd = :wd " +
            "ORDER BY year desc, month desc, day desc, startHour desc, startMinute desc")
    List<ColoredTravel> getAllTravelsOn(int line, int wd);

    @Query("SELECT V.*, L.color FROM viaje V LEFT JOIN lines L ON V.linea = L.number " +
            "WHERE V.linea = :line and V.nombrePdaFin is :dest " +
            "ORDER BY year desc, month desc, day desc, startHour desc, startMinute desc")
    List<ColoredTravel> getAllToDestination(int line, String dest);

    @Query("SELECT V.*, L.color FROM viaje V LEFT JOIN lines L ON V.linea = L.number " +
            "WHERE V.linea = :line and V.ramal = :ramal " +
            "ORDER BY year desc, month desc, day desc, startHour desc, startMinute desc")
    List<ColoredTravel> getAllFromRamal(int line, String ramal);

    @Query("SELECT V.*, L.color FROM viaje V LEFT JOIN lines L ON V.linea = L.number " +
            "WHERE V.linea = :line and V.ramal is null " +
            "ORDER BY year desc, month desc, day desc, startHour desc, startMinute desc")
    List<ColoredTravel> getAllFromNoRamal(int line);

    @Query("SELECT * FROM viaje where id is :travelId LIMIT 1")
    Viaje getById(long travelId);

    @Query("SELECT MAX(costo) FROM viaje where nombrePdaInicio is :inicio and nombrePdaFin is :fin")
    Double getMaxPrice(String inicio, String fin);

    @Query("SELECT costo FROM viaje " +
            "where nombrePdaInicio is :inicio and nombrePdaFin is :fin order by id desc")
    Double getLastPrice(String inicio, String fin);

    @Query("SELECT V.*, L.color FROM viaje V LEFT JOIN lines L ON V.linea = L.number " +
            "where nombrePdaInicio = :stopName and linea is NOT NULL " +
            "and (startHour * 60 + startMinute) between (:target - 5) and (:target + 95) " +
            "order by startHour, year desc, month desc, day desc limit :cant")
    List<ColoredTravel> getNextArrivals(String stopName, int target, int cant);

    @Query("SELECT * FROM Viaje where wd = 0")
    List<Viaje> getUndefinedWeekDays();

    @Query("SELECT v.id, (p1.latitud - p2.latitud) as xDiff, (p1.longitud - p2.longitud) as yDiff " +
            "FROM Viaje v " +
            "inner join Parada p1 on v.nombrePdaInicio = p1.nombre " +
            "inner join Parada p2 on v.nombrePdaFin = p2.nombre")
    List<TravelDistance> getTravelDistances();

    @Query("SELECT MAX(id) from viaje")
    Long getLastTravelId();

    @Query("SELECT COUNT(*) FROM Viaje WHERE (tipo is not :exceptedType) and " +
            "year = :year and month = :month and day = :day " +
            "and (startHour * 60 + startMinute between :start and :end)")
    int countTravelsInTimeRange(int year, int month, int day, int start, int end, int exceptedType);

    // ------------------------------- LIVE --------------------------------------------

    // Comentario: solo se devuelve UN viaje INCOMPLETO empezado HOY, se verifica hora y minuto
    @Query("SELECT V.*, L.color FROM viaje V LEFT JOIN lines L ON V.linea = L.number " +
            "where endHour is null and year = :y and month = :m and day = :d " +
            "and (startHour*60+startMinute) < :currentTs " +
            "order by startHour desc, startMinute desc limit 1")
    ColoredTravel getCurrentTravel(int y, int m, int d, int currentTs);

    @Query("SELECT v.id, (p1.latitud - p2.latitud) as xDiff, (p1.longitud - p2.longitud) as yDiff " +
            "FROM Viaje v " +
            "inner join Parada p1 on v.nombrePdaInicio = p1.nombre " +
            "inner join Parada p2 on v.nombrePdaFin = p2.nombre " +
            "where v.id = :id ")
    TravelDistance getTravelDistanceFromId(long id);

    @Query("SELECT AVG((endHour-startHour)*60 + (endMinute-startMinute)) FROM Viaje " +
            "where ramal is null and nombrePdaInicio = :startStop and nombrePdaFin = :endStop")
    int getAverageTravelDuration(String startStop, String endStop);

    @Query("SELECT AVG((endHour-startHour)*60 + (endMinute-startMinute)) FROM Viaje " +
            "where ramal = :ramal and nombrePdaInicio = :startStop and nombrePdaFin = :endStop")
    int getAverageTravelDurationWithRamal(String startStop, String endStop, String ramal);

    @Query("SELECT * FROM Viaje where endHour is not null and linea is not :excludedLine and " +
            "nombrePdaInicio = :startStop and nombrePdaFin is not :excludedEndStop " +
            "order by RANDOM() limit 1")
    Viaje getCompletedTravelFrom(String startStop, String excludedEndStop, Integer excludedLine);

    @Query("SELECT P1.latitud as start_x, P1.longitud as start_y, " +
            "P2.latitud as end_x, P2.longitud as end_y, " +
            "(V.startHour * 60 + V.startMinute) as start_time," +
            "(V.endHour * 60 + V.endMinute) as end_time " +
            "FROM Viaje V " +
            "INNER JOIN Parada P1 ON P1.nombre = V.nombrePdaInicio " +
            "INNER JOIN Parada P2 on P2.nombre = V.nombrePdaFin " +
            "where linea = :linea and endHour is not null order by V.id DESC limit 1")
    TravelStats getLastFinishedTravelFromLine(int linea);

    @Query("SELECT P1.latitud as start_x, P1.longitud as start_y, " +
            "P2.latitud as end_x, P2.longitud as end_y, " +
            "(V.startHour * 60 + V.startMinute) as start_time," +
            "(V.endHour * 60 + V.endMinute) as end_time " +
            "FROM Viaje V " +
            "INNER JOIN Parada P1 ON P1.nombre = V.nombrePdaInicio " +
            "INNER JOIN Parada P2 on P2.nombre = V.nombrePdaFin " +
            "where linea is null and endHour is not null order by V.id DESC limit 1")
    TravelStats getLastFinishedTrainTravel();

    // ------------------------------ AUTOCOMPLETE CREATION -------------------------------------

    // Atajo: busca el viaje más probable a realizar desde la ubicación actual
    @Query("SELECT * FROM Viaje where nombrePdaInicio = :targetStart and startHour >= :sinceHour " +
            "group by linea, ramal, nombrePdaInicio, nombrePdaFin having count(*) > 2 " +
            "order by COUNT(*) desc limit 1")
    Viaje getLikelyTravelFrom(String targetStart, int sinceHour);

    @Query("SELECT * FROM Viaje where tipo = :type and " +
            "nombrePdaInicio = :targetStart and startHour >= :sinceHour " +
            "group by linea, ramal, nombrePdaInicio, nombrePdaFin having count(*) > 2 " +
            "order by COUNT(*) desc limit 1")
    Viaje getLikelyTravelFromUsingType(String targetStart, int sinceHour, int type);

    @Query("SELECT DISTINCT ramal FROM Viaje where ramal like '___%' order by ramal")
    List<String> getAllRamals();

    @Query("SELECT DISTINCT ramal FROM Viaje where linea = :line and ramal like '___%' order by ramal")
    List<String> getAllRamalsFromLine(int line);

    // ------------------------------ TRAVEL STATS -----------------------------------

    @Query("SELECT MAX((endHour-startHour)*60 + (endMinute-startMinute)) FROM Viaje " +
            "where linea = :line and nombrePdaInicio = :startStop and nombrePdaFin = :endStop")
    Integer getMaxTravelDuration(int line, String startStop, String endStop);

    @Query("SELECT MIN((endHour-startHour)*60 + (endMinute-startMinute)) FROM Viaje " +
            "where linea = :line and nombrePdaInicio = :startStop and nombrePdaFin = :endStop")
    Integer getMinTravelDuration(int line, String startStop, String endStop);

    @Query("SELECT MIN((endHour-startHour)*60 + (endMinute-startMinute)) FROM Viaje " +
            "where nombrePdaInicio = :startStop and nombrePdaFin = :endStop")
    Integer getTrainMinTravelDuration(String startStop, String endStop);

    // ------------------------------ LINE STATS -------------------------------

    @Query("SELECT P1.latitud as start_x, P1.longitud as start_y, " +
            "P2.latitud as end_x, P2.longitud as end_y, " +
            "(V.startHour * 60 + V.startMinute) as start_time," +
            "(V.endHour * 60 + V.endMinute) as end_time " +
            "FROM Viaje V " +
            "INNER JOIN Parada P1 ON P1.nombre = V.nombrePdaInicio " +
            "INNER JOIN Parada P2 on P2.nombre = V.nombrePdaFin " +
            "where linea is null and endHour is not null " +
            "order by year desc, month desc, day desc limit 10")
    List<TravelStats> getRecentFinishedTravelsFromTrains();

    @Query("SELECT P1.latitud as start_x, P1.longitud as start_y, " +
            "P2.latitud as end_x, P2.longitud as end_y, " +
            "(V.startHour * 60 + V.startMinute) as start_time," +
            "(V.endHour * 60 + V.endMinute) as end_time " +
            "FROM Viaje V " +
            "INNER JOIN Parada P1 ON P1.nombre = V.nombrePdaInicio " +
            "INNER JOIN Parada P2 on P2.nombre = V.nombrePdaFin " +
            "where linea = :linea and endHour is not null " +
            "order by year desc, month desc, day desc limit 10")
    List<TravelStats> getRecentFinishedTravelsFromLine(int linea);

    @Query("SELECT P1.latitud as start_x, P1.longitud as start_y, " +
            "P2.latitud as end_x, P2.longitud as end_y, " +
            "(V.startHour * 60 + V.startMinute) as start_time," +
            "(V.endHour * 60 + V.endMinute) as end_time " +
            "FROM Viaje V " +
            "INNER JOIN Parada P1 ON P1.nombre = V.nombrePdaInicio " +
            "INNER JOIN Parada P2 on P2.nombre = V.nombrePdaFin " +
            "where linea = :linea and endHour is not null")
    List<TravelStats> getAllFinishedTravelsFromLine(int linea);

    @Query("SELECT P1.latitud as start_x, P1.longitud as start_y, " +
            "P2.latitud as end_x, P2.longitud as end_y, " +
            "(V.startHour * 60 + V.startMinute) as start_time," +
            "(V.endHour * 60 + V.endMinute) as end_time " +
            "FROM Viaje V INNER JOIN Parada P1 ON P1.nombre = V.nombrePdaInicio " +
            "INNER JOIN Parada P2 on P2.nombre = V.nombrePdaFin " +
            "where linea = :linea and ramal = :ramal and endHour is not null " +
            "order by year desc, month desc, day desc limit 10")
    List<TravelStats> getRecentFinishedTravelsFromRamal(int linea, String ramal);

    @Query("SELECT P1.latitud as start_x, P1.longitud as start_y, " +
            "P2.latitud as end_x, P2.longitud as end_y, " +
            "(V.startHour * 60 + V.startMinute) as start_time," +
            "(V.endHour * 60 + V.endMinute) as end_time " +
            "FROM Viaje V INNER JOIN Parada P1 ON P1.nombre = V.nombrePdaInicio " +
            "INNER JOIN Parada P2 on P2.nombre = V.nombrePdaFin " +
            "where linea = :number and nombrePdaFin = :endStop and endHour is not null " +
            "order by year desc, month desc, day desc limit 10")
    List<TravelStats> getRecentFinishedTravelsTo(String endStop, int number);

    @Query("SELECT P1.latitud as start_x, P1.longitud as start_y, " +
            "P2.latitud as end_x, P2.longitud as end_y, " +
            "(V.startHour * 60 + V.startMinute) as start_time," +
            "(V.endHour * 60 + V.endMinute) as end_time " +
            "FROM Viaje V INNER JOIN Parada P1 ON P1.nombre = V.nombrePdaInicio " +
            "INNER JOIN Parada P2 on P2.nombre = V.nombrePdaFin " +
            "where linea = :number and V.wd = :wd and endHour is not null " +
            "order by year desc, month desc, day desc limit 10")
    List<TravelStats> getRecentFinishedTravelsOn(int wd, int number);


    // -------------------------- MONTH SUMMARY -------------------------------

    @Query("SELECT SUM(costo) FROM viaje where linea is not null and id > :travelId")
    double getSpentInBusesSince(long travelId);

    @Query("SELECT SUM(costo) FROM viaje where linea is null and id > :travelId")
    double getSpentInTrainsSince(long travelId);

    @Query("SELECT SUM(costo) FROM viaje where linea is not null and month is :month and year is :year")
    double getTotalSpentInBuses(int month, int year);

    @Query("SELECT SUM(costo) FROM viaje where linea is null and month is :month and year is :year")
    double getTotalSpentInTrains(int month, int year);

    @Query("SELECT V.linea, L.color, SUM(V.costo) as suma FROM viaje V " +
            "LEFT JOIN lines L ON V.linea = L.number " +
            "where linea is not null and month is :month and year is :year " +
            " group by linea order by 3 desc limit 3")
    List<PriceSum> getMostExpensiveBus(int month, int year);


}
