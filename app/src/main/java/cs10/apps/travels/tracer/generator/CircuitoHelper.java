package cs10.apps.travels.tracer.generator;

import java.util.ArrayList;
import java.util.List;

import cs10.apps.common.android.CS_Time;
import cs10.apps.travels.tracer.db.MiDB;
import cs10.apps.travels.tracer.model.roca.HorarioTren;
import cs10.apps.travels.tracer.model.roca.ServicioTren;

public abstract class CircuitoHelper {
    private final DelayData delayData;
    protected final Station[] LISTADO_PARADAS;

    public CircuitoHelper(DelayData delayData, Station[] LISTADO_PARADAS) {
        this.delayData = delayData;
        this.LISTADO_PARADAS = LISTADO_PARADAS;
    }

    public void create(Station inicio, int hora, int minuto, Station fin, boolean haciaAdelante, MiDB db){
        ArrayList<Station> stations = new ArrayList<>();
        int aux = haciaAdelante ? 0 : LISTADO_PARADAS.length - 1;
        int delta = haciaAdelante ? 1 : -1;

        // busco la parada inicial y la agrego al recorrido
        while (LISTADO_PARADAS[aux] != inicio) aux += delta;
        stations.add(LISTADO_PARADAS[aux]);

        // busco la parada final (a partir de la siguiente)
        aux += delta;

        // voy agregando cada estación intermedia al recorrido
        while (LISTADO_PARADAS[aux] != fin){
            Station actual = LISTADO_PARADAS[aux];
            stations.add(actual);
            aux += delta;
        }

        // agrego la estación final al recorrido
        stations.add(LISTADO_PARADAS[aux]);

        // una vez confirmado que existen las paradas, crear servicio
        ServicioTren servicio = new ServicioTren();
        servicio.setRamal(fin.getSimplified());
        servicio.setCabecera(inicio.getNombre());
        servicio.setHora(hora);
        servicio.setMinuto(minuto);
        long id = db.servicioDao().insert(servicio);

        createHorarios(stations, id, hora, minuto, db);
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
