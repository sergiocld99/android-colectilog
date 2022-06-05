package cs10.apps.travels.tracer.model.roca;

/*
    Un arribo se identifica por el nombre de la estación y la hora. Sin embargo, usar solo eso
    para la clave primaria no alcanza: no permite que 2 trenes estén en una estación a la vez.

    Para saber cuáles son las siguientes estaciones del recorrido no es necesario agregar
    un atributo "ordinal". Mediante el ID de servicio, se obtienen los arribos a cada estación
    para ese recorrido en particular, y los siguientes se filtran fácilmente por hora y minuto,
    pudiendo ordenar los resultados y limitarlo al primero para obtener solo la siguiente.
 */

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;

@Entity(primaryKeys = {"service", "hour", "minute"},
        foreignKeys = {@ForeignKey(entity = ServicioTren.class, parentColumns = "id", childColumns = "service")})
public class HorarioTren {

    private long service;
    private int hour, minute;
    private String station;

    @Ignore
    private HorarioTren combination;

    @Ignore
    private String combinationRamal;

    public String getStation() {
        return station;
    }

    public void setStation(String station) {
        this.station = station;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public long getService() {
        return service;
    }

    public void setService(long service) {
        this.service = service;
    }

    public HorarioTren getCombination() {
        return combination;
    }

    public void setCombination(HorarioTren combination) {
        this.combination = combination;
    }

    public String getCombinationRamal() {
        return combinationRamal;
    }

    public void setCombinationRamal(String combinationRamal) {
        this.combinationRamal = combinationRamal;
    }
}
