package cs10.apps.travels.tracer.model;

import android.location.Location;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import cs10.apps.common.android.DoubleHistory;
import cs10.apps.common.android.Localizable;
import cs10.apps.common.android.NumberUtils;

@Entity
public class Parada implements Comparable<Parada>, Localizable {

    @PrimaryKey @NonNull
    private String nombre = "Nombre de ejemplo";

    private double latitud;
    private double longitud;
    private int tipo;

    @Ignore
    private double deltaX;

    @Ignore
    private double deltaY;

    @Ignore
    private final DoubleHistory doubleHistory = new DoubleHistory(0.1);

    @Ignore
    private Zone zone;

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

    public void updateDistance(Location location){
        this.updateDistance(location.getLatitude(), location.getLongitude());
    }

    public void updateDistance(double currentX, double currentY){
        setDeltaX(latitud - currentX);
        setDeltaY(longitud - currentY);
        doubleHistory.updateNew(getDistance());
    }

    public DoubleHistory getDoubleHistory() {
        return doubleHistory;
    }

    public double getDistance(){
        double hyp = NumberUtils.Companion.hyp(deltaX, deltaY);
        return NumberUtils.Companion.coordsDistanceToKm(hyp);
    }

    public void setZone(Zone zone) {
        this.zone = zone;
    }

    public Zone getZone() {
        return zone;
    }

    @Override
    public int compareTo(@NonNull Parada parada) {
        return Double.compare(this.doubleHistory.comparisonDiff(), parada.doubleHistory.comparisonDiff());
    }

    @Override @NonNull
    public String toString() {
        return nombre;
    }

    @Override
    public double getX() {
        return getLatitud();
    }

    @Override
    public double getY() {
        return getLongitud();
    }
}
