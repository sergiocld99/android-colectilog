package cs10.apps.travels.tracer.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "formadoPor")
public class FormacionCircuito {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public int circuito;
    public int estacion;
    public int orden;

    public FormacionCircuito(){}

    @Ignore
    public FormacionCircuito(@NonNull Circuito circuito, @NonNull Estacion estacion, int orden){
        this.circuito = circuito.id;
        this.estacion = estacion.cod;
        this.orden = orden;
    }
}
