package cs10.apps.travels.tracer.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;

@Entity(tableName = "estaEn", primaryKeys = {"coche", "estacion", "tipoDia"})
public class Horario {

    public int coche;
    public int estacion;
    public int tipoDia;
    public int hora, minuto;

    public Horario(){}

    @Ignore
    public Horario(@NonNull Tren tren, @NonNull Estacion estacion, @NonNull TipoDia tipoDia, int hora, int minuto){
        this.coche = tren.coche;
        this.estacion = estacion.cod;
        this.tipoDia = tipoDia.ordinal();
        this.hora = hora;
        this.minuto = minuto;
    }
}
