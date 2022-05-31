package cs10.apps.travels.tracer.db;

import androidx.room.Dao;
import androidx.room.Insert;

import cs10.apps.travels.tracer.model.roca.HorarioTren;
import cs10.apps.travels.tracer.model.roca.ServicioTren;

@Dao
public interface ServicioDao {

    @Insert
    long insert(ServicioTren servicioTren);

    @Insert
    void insertHorario(HorarioTren horarioTren);
}
