package cs10.apps.travels.tracer.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "comunicaCon")
public class Comunicacion {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "actual")
    public int estacionActual;

    @ColumnInfo(name = "siguiente")
    public int estacionSiguiente;

    public Comunicacion(){}

    @Ignore
    public Comunicacion(@NonNull Estacion actual, @NonNull Estacion siguiente){
        this.estacionActual = actual.cod;
        this.estacionSiguiente = siguiente.cod;
    }
}
