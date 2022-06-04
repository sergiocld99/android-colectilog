package cs10.apps.travels.tracer.generator;

import java.util.List;

import cs10.apps.common.android.CS_Time;
import cs10.apps.travels.tracer.db.MiDB;
import cs10.apps.travels.tracer.model.roca.HorarioTren;

public abstract class CircuitoHelper {
    private final DelayData delayData;

    public CircuitoHelper(DelayData delayData) {
        this.delayData = delayData;
    }

    protected void createHorarios(List<Station> confirmedStations, long id, int hora, int minuto, MiDB db){
        // variables auxiliares
        Station anterior = null;
        CS_Time time = new CS_Time();
        time.setHour(hora);
        time.setMinute(minuto);

        // para cada estación del recorrido (ya se quitaron las que no corresponden)
        for (Station actual : confirmedStations) {
            // desplazar tiempo, excepto si es la inicial
            if (anterior != null) time.add(delayData.getDelay(anterior, actual));

            HorarioTren horario = new HorarioTren();
            horario.setService(id);
            horario.setStation(actual.getNombre());
            horario.setHour(time.getHour());
            horario.setMinute(time.getMinute());
            db.servicioDao().insertHorario(horario);

            anterior = actual;
        }
    }
}
