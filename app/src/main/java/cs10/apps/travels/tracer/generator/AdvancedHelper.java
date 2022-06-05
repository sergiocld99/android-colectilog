package cs10.apps.travels.tracer.generator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;

import cs10.apps.travels.tracer.db.MiDB;
import cs10.apps.travels.tracer.model.roca.ServicioTren;

public abstract class AdvancedHelper extends CircuitoHelper {

    private final Station[] PARADAS_SALTEADAS_DIRECTO;

    private final Station[] PARADAS_SALTEADAS_GERLI = {
            Station.YRIGOYEN, Station.GERLI
    };

    public AdvancedHelper(DelayData delayData, Station[] LISTADO_PARADAS, Station[] PARADAS_SALTEADAS_DIRECTO) {
        super(delayData, LISTADO_PARADAS);
        this.PARADAS_SALTEADAS_DIRECTO = PARADAS_SALTEADAS_DIRECTO;
    }

    @Override
    public void create(Station inicio, int hora, int minuto, Station fin, boolean haciaAdelante, MiDB db) {
        create(inicio, hora, minuto, fin, haciaAdelante, null, db);
    }

    public void create(Station estacionInicio, int horaInicio, int minutoInicio, Station estacionFinal,
                       boolean haciaAdelante, Salteo salteo, MiDB db){

        // CORRECCIÓN IMPORTANTE: DEBE CONSIDERARSE EL SENTIDO PARA SALTEAR ESTACIONES
        ArrayList<Station> stations = new ArrayList<>();
        LinkedList<Station> skippedStations = getSkippedFor(salteo, haciaAdelante);
        int aux = haciaAdelante ? 0 : LISTADO_PARADAS.length - 1;
        int delta = haciaAdelante ? 1 : -1;

        // busco la parada inicial y la agrego al recorrido
        while (LISTADO_PARADAS[aux] != estacionInicio) aux += delta;
        stations.add(LISTADO_PARADAS[aux]);

        // busco la parada final (a partir de la siguiente)
        aux += delta;

        while (LISTADO_PARADAS[aux] != estacionFinal){
            Station actual = LISTADO_PARADAS[aux];

            // agrego la estación consultada si corresponde
            // CORRECCIÓN IMPORTANTE PARA AVELLANEDA, caso DIRECTO...
            // ... En el caso "haciaAtras" (via Quilmes), borrar solo en la segunda vez
            if (!skippedStations.isEmpty() && skippedStations.getFirst() == actual) skippedStations.removeFirst();
            else stations.add(actual);

            aux += delta;
        }

        // agrego la estación final al recorrido
        stations.add(LISTADO_PARADAS[aux]);

        // una vez confirmado que existen las paradas, crear servicio
        ServicioTren servicio = new ServicioTren();
        servicio.setRamal(getRamalFor(estacionInicio, estacionFinal, haciaAdelante, salteo));
        servicio.setCabecera(estacionInicio.getNombre());
        servicio.setHora(horaInicio);
        servicio.setMinuto(minutoInicio);
        long id = db.servicioDao().insert(servicio);

        super.createHorarios(stations, id, horaInicio, minutoInicio, db);
    }

    protected LinkedList<Station> getSkippedFor(Salteo salteo, boolean haciaAdelante){
        if (salteo == null) return new LinkedList<>();
        LinkedList<Station> resultado;

        switch (salteo){
            case DIRECTO:
                resultado = new LinkedList<>(Arrays.asList(PARADAS_SALTEADAS_DIRECTO));
                break;
            case GERLI:
                resultado = new LinkedList<>(Arrays.asList(PARADAS_SALTEADAS_GERLI));
                break;
            default:
                resultado = new LinkedList<>();
                break;
        }

        if (!haciaAdelante) Collections.reverse(resultado);     // Caso Via Quilmes (DIRECTOS)
        return resultado;
    }

    protected abstract String getRamalFor(Station inicio, Station fin, boolean haciaAdelante, Salteo salteo);
}
