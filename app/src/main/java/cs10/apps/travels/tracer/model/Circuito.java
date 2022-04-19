package cs10.apps.travels.tracer.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "circuitos")
public class Circuito {

    @PrimaryKey
    public int id;

    public String nombre;

    public Circuito(){}

    @Ignore
    public Circuito(@NonNull String nombre){
        this.nombre = nombre;
        this.id = nombre.hashCode();
    }
}
