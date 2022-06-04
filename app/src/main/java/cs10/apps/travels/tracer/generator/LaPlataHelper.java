package cs10.apps.travels.tracer.generator;

import java.util.ArrayList;

import cs10.apps.travels.tracer.db.MiDB;
import cs10.apps.travels.tracer.model.roca.ServicioTren;

// Más sencillo que Via Circuito, no hay estaciones a saltear
public class LaPlataHelper extends CircuitoHelper {

    private final Station[] LISTADO_PARADAS = {
            Station.PLAZA, Station.AVELLANEDA, Station.SARANDI, Station.DOMINICO, Station.WILDE,
            Station.DON_BOSCO, Station.BERNAL, Station.QUILMES, Station.EZPELETA, Station.BERA,
            Station.PLATANOS, Station.HUDSON, Station.PEREYRA, Station.VILLA_ELISA,
            Station.CITY_BELL, Station.GONNET, Station.RINGUELET, Station.TOLOSA, Station.LA_PLATA
    };

    public LaPlataHelper(DelayData delayData) {
        super(delayData);
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
        servicio.setRamal(fin.getNombre().replace("Estación","").trim());
        servicio.setCabecera(inicio.getNombre());
        servicio.setHora(hora);
        servicio.setMinuto(minuto);
        long id = db.servicioDao().insert(servicio);

        super.createHorarios(stations, id, hora, minuto, db);
    }
}
