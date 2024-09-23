package cs10.apps.travels.tracer.model.joins;

import androidx.room.Ignore;

import cs10.apps.travels.tracer.utils.Utils;
import cs10.apps.travels.tracer.model.Parada;

public class ScheduledParada extends Parada {

    private Integer linea;
    private int startHour, startMinute, color;
    private String ramal, nombrePdaInicio, nombrePdaFin;

    @Ignore
    private double costo;

    @Ignore
    public boolean switched;

    public String getNombrePdaInicio() {
        return nombrePdaInicio;
    }

    public void setNombrePdaInicio(String nombrePdaInicio) {
        this.nombrePdaInicio = nombrePdaInicio;
    }

    public String getNombrePdaFin() {
        return nombrePdaFin;
    }

    public void setNombrePdaFin(String nombrePdaFin) {
        this.nombrePdaFin = nombrePdaFin;
    }

    public double getCosto() {
        return costo;
    }

    public void setCosto(double costo) {
        this.costo = costo;
    }

    public Integer getLinea() {
        return linea;
    }

    public void setLinea(Integer linea) {
        this.linea = linea;
    }

    public int getStartHour() {
        return startHour;
    }

    public void setStartHour(int startHour) {
        this.startHour = startHour;
    }

    public int getStartMinute() {
        return startMinute;
    }

    public void setStartMinute(int startMinute) {
        this.startMinute = startMinute;
    }

    public String getRamal() {
        return ramal;
    }

    public void setRamal(String ramal) {
        this.ramal = ramal;
    }

    public String getTransportInfo(){
        if (getLinea() == null) return "Tren";
        return getLinea() + (ramal == null ? "" : " (" + getRamal() + ")");
    }

    public String getLineaAsString(){
        if (getLinea() == null) return "Tren";
        return String.valueOf(getLinea());
    }

    public String getNextArrival(){
        return getStartHour() + ":" + Utils.twoDecimals(getStartMinute());
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }
}
