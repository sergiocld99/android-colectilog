package cs10.apps.travels.tracer.db;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

import cs10.apps.travels.tracer.model.prices.TarifaBus;
import cs10.apps.travels.tracer.model.prices.TarifaTren;

@Dao
public interface TarifasDao {

    @Query("SELECT * FROM TarifaTren where inicio is :inicio")
    List<TarifaTren> getForTrain(String inicio);

    @Query("SELECT * FROM TarifaBus where inicio is :inicio")
    List<TarifaBus> getForBus(String inicio);
}
