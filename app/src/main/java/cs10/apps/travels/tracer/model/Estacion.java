package cs10.apps.travels.tracer.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "estaciones")
public class Estacion {

    @PrimaryKey
    public int cod;

    public String nombre;

    public Estacion(){}

    @Ignore
    public Estacion(@NonNull String nombre) {
        this.nombre = nombre;
        this.cod = nombre.hashCode();
    }
}
