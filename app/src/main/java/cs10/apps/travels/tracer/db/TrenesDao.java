package cs10.apps.travels.tracer.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import cs10.apps.travels.tracer.model.Circuito;
import cs10.apps.travels.tracer.model.Comunicacion;
import cs10.apps.travels.tracer.model.Estacion;
import cs10.apps.travels.tracer.model.FormacionCircuito;
import cs10.apps.travels.tracer.model.Tren;
import cs10.apps.travels.tracer.model.Horario;

@Dao
public interface TrenesDao {

    @Insert
    void insert(Circuito circuito);

    @Insert
    void insert(Comunicacion comunicacion);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Estacion estacion);

    @Insert
    void insert(FormacionCircuito formacionCircuito);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Tren tren);

    @Insert
    void insert(Horario horario);

    @Query("SELECT * FROM circuitos WHERE(nombre IS :name) LIMIT 1")
    Circuito getCircuito(String name);

    @Query("SELECT * FROM estaciones")
    List<Estacion> getAllStations();

    @Query("SELECT estaciones.* FROM estaciones, formadoPor " +
            "WHERE(estaciones.cod IS formadoPor.estacion AND circuito IS :circuitoId) " +
            "ORDER BY orden")
    List<Estacion> getAllStations(int circuitoId);

    @Query("SELECT * FROM trenes WHERE(coche IS :nroCoche) LIMIT 1")
    Tren getTren(int nroCoche);

    @Query("DELETE FROM trenes")
    void deleteAllTrains();

    @Query("DELETE FROM estaEn")
    void deleteAllSchedules();
}
