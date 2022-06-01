package cs10.apps.travels.tracer.db.filler;

/*
    Esta clase pretende ser una versión mejorada de la primera (ViaCircuitoFiller), para
    permitir insertar servicios que no son completos o que no respetan un horario periódico.
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;

import cs10.apps.common.android.CS_Time;
import cs10.apps.travels.tracer.db.MiDB;
import cs10.apps.travels.tracer.model.roca.HorarioTren;
import cs10.apps.travels.tracer.model.roca.ServicioTren;

public class ViaCircuitoHelper {

    // Todas las paradas existentes, caso Via Circuito por Temperley > Bosques > Quilmes
    public static final Station[] LISTADO_PARADAS = new Station[]{
            Station.PLAZA, Station.YRIGOYEN, Station.AVELLANEDA, Station.GERLI, Station.LANUS,
            Station.ESCALADA, Station.BANFIELD, Station.LOMAS, Station.TEMPERLEY, Station.MARMOL,
            Station.CALZADA, Station.CLAYPOLE, Station.ARDIGO, Station.VARELA, Station.ZEBALLOS,
            Station.BOSQUES, Station.SOURIGUES, Station.RANELAGH, Station.VILLA_ESP, Station.BERA,
            Station.EZPELETA, Station.QUILMES, Station.BERNAL, Station.DON_BOSCO, Station.WILDE,
            Station.DOMINICO, Station.SARANDI, Station.AVELLANEDA, Station.PLAZA
    };

    public static final Station[] PARADAS_SALTEADAS_DIRECTO = new Station[]{
            Station.YRIGOYEN, Station.AVELLANEDA, Station.GERLI,
            Station.ESCALADA, Station.BANFIELD, Station.TEMPERLEY
    };

    public static final Station[] PARADAS_SALTEADAS_NORMAL = new Station[]{
            Station.YRIGOYEN, Station.GERLI
    };

    private final int[][] DEMORA_MINUTOS = new int[Station.values().length][Station.values().length];

    public ViaCircuitoHelper(){
        add(Station.PLAZA, Station.YRIGOYEN, 37-32);
        add(Station.YRIGOYEN, Station.AVELLANEDA, 40-37);
        add(Station.AVELLANEDA, Station.GERLI, 43-40);
        add(Station.GERLI, Station.LANUS, 47-43);
        add(Station.LANUS, Station.ESCALADA, 50-47);
        add(Station.ESCALADA, Station.BANFIELD, 53-50);
        add(Station.BANFIELD, Station.LOMAS, 57-53);
        add(Station.LOMAS, Station.TEMPERLEY, 61-57);
        add(Station.TEMPERLEY, Station.MARMOL, 4-1);
        add(Station.MARMOL, Station.CALZADA, 7-4);
        add(Station.CALZADA, Station.CLAYPOLE, 10-7);
        add(Station.CLAYPOLE, Station.ARDIGO, 14-10);
        add(Station.ARDIGO, Station.VARELA, 19-14);
        add(Station.VARELA, Station.ZEBALLOS, 22-19);
        add(Station.ZEBALLOS, Station.BOSQUES, 26-22);

        add(Station.BOSQUES, Station.SOURIGUES, 20-13);
        add(Station.SOURIGUES, Station.RANELAGH, 24-20);
        add(Station.RANELAGH, Station.VILLA_ESP, 28-24);
        add(Station.VILLA_ESP, Station.BERA, 32-28);
        add(Station.BERA, Station.EZPELETA, 35-32);
        add(Station.EZPELETA, Station.QUILMES, 41-35);
        add(Station.QUILMES, Station.BERNAL, 44-41);
        add(Station.BERNAL, Station.DON_BOSCO, 47-44);
        add(Station.DON_BOSCO, Station.WILDE, 49-47);
        add(Station.WILDE, Station.DOMINICO, 52-49);
        add(Station.DOMINICO, Station.SARANDI, 55-52);
        add(Station.SARANDI, Station.AVELLANEDA, 60-55);
        add(Station.AVELLANEDA, Station.PLAZA, 6);

        add(Station.PLAZA, Station.LANUS, 37-26);       // Servicio Directo
        add(Station.LANUS, Station.LOMAS, 45-37);       // Servicio Directo
        add(Station.LOMAS, Station.MARMOL, 51-45);      // No para en Temperley

        add(Station.AVELLANEDA, Station.LANUS, 14-9);   // No para en Gerli
    }

    // add both sides
    private void add(Station s1, Station s2, int offsetTime){
        addOnly(s1, s2, offsetTime);
        addOnly(s2, s1, offsetTime);
    }

    private void addOnly(Station s1, Station s2, int offsetTime){
        DEMORA_MINUTOS[s1.ordinal()][s2.ordinal()] = offsetTime;
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

        // variables auxiliares
        Station anterior = null;
        CS_Time time = new CS_Time();
        time.setHour(horaInicio);
        time.setMinute(minutoInicio);

        // para cada estación del recorrido (ya se quitaron las que no corresponden)
        for (Station actual : stations) {
            // desplazar tiempo, excepto si es la inicial
            if (anterior != null) time.add(DEMORA_MINUTOS[anterior.ordinal()][actual.ordinal()]);

            HorarioTren horario = new HorarioTren();
            horario.setService(id);
            horario.setStation(actual.getNombre());
            horario.setHour(time.getHour());
            horario.setMinute(time.getMinute());
            db.servicioDao().insertHorario(horario);

            anterior = actual;
        }
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
            if (haciaAdelante) return "Via Temperley > Quilmes";
            return "Via Quilmes > Temperley";
        }

        if (inicio == Station.BOSQUES || fin == Station.BOSQUES){
            if (salteo == Salteo.DIRECTO) return "Bosques T (Directo)";
            return "Bosques T";
        }

        if (inicio == Station.VARELA || fin == Station.VARELA){
            if (salteo == Salteo.DIRECTO) return "Varela T (Directo)";
            return "Varela T";
        }

        return fin.getNombre().replace("Estación","").trim();
    }
}
