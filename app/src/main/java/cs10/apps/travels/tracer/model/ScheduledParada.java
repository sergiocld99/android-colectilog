package cs10.apps.travels.tracer.model;

import androidx.room.Ignore;

import cs10.apps.travels.tracer.Utils;

public class ScheduledParada extends Parada {

    private Integer linea;
    private int startHour, startMinute;
    private String ramal;

    @Ignore
    public boolean switched;

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

    public String getNextArrival(){
        return getStartHour() + ":" + Utils.twoDecimals(getStartMinute());
    }
}
