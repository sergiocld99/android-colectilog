package cs10.apps.travels.tracer.model.roca;

/*
    Un arribo se identifica por el nombre de la estación y la hora. Para este primer modelo
    suponemos que no pueden llegar 2 trenes en el mismo minuto a una misma estación.

    Para saber cuáles son las siguientes estaciones del recorrido no es necesario agregar
    un atributo "ordinal". Mediante el ID de servicio, se obtienen los arribos a cada estación
    para ese recorrido en particular, y los siguientes se filtran fácilmente por hora y minuto,
    pudiendo ordenar los resultados y limitarlo al primero para obtener solo la siguiente.
 */

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;

@Entity(primaryKeys = {"station", "hour", "minute"},
        foreignKeys = {@ForeignKey(entity = ServicioTren.class, parentColumns = "id", childColumns = "service")})
public class HorarioTren {

    @NonNull
    private String station = "Estación";

    private int hour, minute;
    private long service;

    @NonNull
    public String getStation() {
        return station;
    }

    public void setStation(@NonNull String station) {
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

    public void setTime(int hour, int minute) {
        setHour(hour);
        setMinute(minute);
    }
}
