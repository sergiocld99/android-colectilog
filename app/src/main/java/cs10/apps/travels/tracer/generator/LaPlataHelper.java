package cs10.apps.travels.tracer.generator;

// Más sencillo que Via Circuito, no hay estaciones a saltear
public class LaPlataHelper extends CircuitoHelper {

    private static final Station[] LISTADO_PARADAS = {
            Station.PLAZA, Station.AVELLANEDA, Station.SARANDI, Station.DOMINICO, Station.WILDE,
            Station.DON_BOSCO, Station.BERNAL, Station.QUILMES, Station.EZPELETA, Station.BERA,
            Station.PLATANOS, Station.HUDSON, Station.PEREYRA, Station.VILLA_ELISA,
            Station.CITY_BELL, Station.GONNET, Station.RINGUELET, Station.TOLOSA, Station.LA_PLATA
    };

    public LaPlataHelper(DelayData delayData) {
        super(delayData, LISTADO_PARADAS);
    }
}
