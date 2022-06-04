package cs10.apps.travels.tracer.generator;

/*
    Esta clase pretende ser una versión mejorada de la primera (ViaCircuitoFiller), para
    permitir insertar servicios que no son completos o que no respetan un horario periódico.
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;

import cs10.apps.travels.tracer.db.MiDB;
import cs10.apps.travels.tracer.model.roca.ServicioTren;

public class ViaCircuitoHelper extends CircuitoHelper {

    // Todas las paradas existentes, caso Via Circuito por Temperley > Bosques > Quilmes
    private final Station[] LISTADO_PARADAS = {
            Station.PLAZA, Station.YRIGOYEN, Station.AVELLANEDA, Station.GERLI, Station.LANUS,
            Station.ESCALADA, Station.BANFIELD, Station.LOMAS, Station.TEMPERLEY, Station.MARMOL,
            Station.CALZADA, Station.CLAYPOLE, Station.ARDIGO, Station.VARELA, Station.ZEBALLOS,
            Station.BOSQUES, Station.SOURIGUES, Station.RANELAGH, Station.VILLA_ESP, Station.BERA,
            Station.EZPELETA, Station.QUILMES, Station.BERNAL, Station.DON_BOSCO, Station.WILDE,
            Station.DOMINICO, Station.SARANDI, Station.AVELLANEDA, Station.PLAZA
    };

    private final Station[] PARADAS_SALTEADAS_DIRECTO = {
            Station.YRIGOYEN, Station.AVELLANEDA, Station.GERLI,
            Station.ESCALADA, Station.BANFIELD, Station.TEMPERLEY
    };

    private final Station[] PARADAS_SALTEADAS_NORMAL = {
            Station.YRIGOYEN, Station.GERLI
    };

    public ViaCircuitoHelper(DelayData delayData){
        super(delayData);
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

    private LinkedList<Station> getSkippedFor(Salteo salteo, boolean haciaAdelante) {
        if (salteo == null) return new LinkedList<>();
        LinkedList<Station> resultado;

        switch (salteo){
            case DIRECTO:
                resultado = new LinkedList<>(Arrays.asList(PARADAS_SALTEADAS_DIRECTO));
                break;
            case GERLI:
                resultado = new LinkedList<>(Arrays.asList(PARADAS_SALTEADAS_NORMAL));
                break;
            default:
                resultado = new LinkedList<>();
                break;
        }

        if (!haciaAdelante) Collections.reverse(resultado);     // Caso Via Quilmes (DIRECTOS)
        return resultado;
    }

    private String getRamalFor(Station inicio, Station fin, boolean haciaAdelante, Salteo salteo){
        if (inicio == Station.PLAZA && fin == Station.PLAZA){
            if (haciaAdelante) return "Bosques T > Quilmes";
            return "Bosques Q > Temperley";
        }

        if (fin == Station.PLAZA){
            if (salteo == Salteo.DIRECTO) return "Plaza C (Directo)";
            return "Via Temperley";
        }

        if (fin == Station.BOSQUES){
            if (salteo == Salteo.DIRECTO) return "Bosques T (Directo)";
            return "Bosques T";
        }

        if (fin == Station.VARELA){
            if (salteo == Salteo.DIRECTO) return "Varela T (Directo)";
            return "Varela T";
        }

        return fin.getNombre().replace("Estación","").trim();
    }
}
