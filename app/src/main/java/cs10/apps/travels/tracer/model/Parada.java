package cs10.apps.travels.tracer.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity
public class Parada implements Comparable<Parada> {

    @PrimaryKey @NonNull
    private String nombre = "Nombre de ejemplo";

    private double latitud;
    private double longitud;
    private int tipo;

    @Ignore
    private double deltaX;

    @Ignore
    private double deltaY;

    @NonNull
    public String getNombre() {
        return nombre;
    }

    public void setNombre(@NonNull String nombre) {
        this.nombre = nombre;
    }

    public double getLatitud() {
        return latitud;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }

    public int getTipo() {
        return tipo;
    }

    public void setTipo(int tipo) {
        this.tipo = tipo;
    }

    public double getDeltaX() {
        return deltaX;
    }

    public double getDeltaY() {
        return deltaY;
    }

    public void setDeltaX(double deltaX){
        this.deltaX = deltaX;
    }

    public void setDeltaY(double deltaY){
        this.deltaY = deltaY;
    }

    public double getDistance(){
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY) * 91.97;
    }

    public String getDistanceInKm(){
        return Math.round(getDistance() * 100) / 100.0 + " km";
    }

    @Override
    public int compareTo(@NonNull Parada parada) {
        return this.nombre.compareTo(parada.nombre);
    }

    @Override @NonNull
    public String toString() {
        return nombre;
    }
}
