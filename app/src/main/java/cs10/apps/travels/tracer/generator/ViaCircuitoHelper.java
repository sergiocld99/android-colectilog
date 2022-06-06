package cs10.apps.travels.tracer.generator;

/*
    Esta clase pretende ser una versión mejorada de la primera (ViaCircuitoFiller), para
    permitir insertar servicios que no son completos o que no respetan un horario periódico.
 */

public class ViaCircuitoHelper extends AdvancedHelper {

    private static final Station[] LISTADO_PARADAS = {
            Station.PLAZA, Station.YRIGOYEN, Station.AVELLANEDA, Station.GERLI, Station.LANUS,
            Station.ESCALADA, Station.BANFIELD, Station.LOMAS, Station.TEMPERLEY, Station.MARMOL,
            Station.CALZADA, Station.CLAYPOLE, Station.ARDIGO, Station.VARELA, Station.ZEBALLOS,
            Station.BOSQUES, Station.SOURIGUES, Station.RANELAGH, Station.VILLA_ESP, Station.BERA,
            Station.EZPELETA, Station.QUILMES, Station.BERNAL, Station.DON_BOSCO, Station.WILDE,
            Station.DOMINICO, Station.SARANDI, Station.AVELLANEDA, Station.PLAZA
    };

    private static final Station[] PARADAS_SALTEADAS_DIRECTO = {
            Station.YRIGOYEN, Station.AVELLANEDA, Station.GERLI,
            Station.ESCALADA, Station.BANFIELD, Station.TEMPERLEY
    };

    public ViaCircuitoHelper(DelayData delayData){
        super(delayData, LISTADO_PARADAS, PARADAS_SALTEADAS_DIRECTO);
    }

    @Override
    protected String getRamalFor(Station inicio, Station fin, boolean haciaAdelante, Salteo salteo){
        if (inicio == Station.PLAZA){
            if (haciaAdelante) {
                if (fin == Station.PLAZA) return Ramal.BOSQUES_T_QUILMES.getNombre();
            } else if (fin == Station.TEMPERLEY || fin == Station.PLAZA){
                return Ramal.BOSQUES_Q_TEMPERLEY.getNombre();
            }
        }

        if (fin == Station.PLAZA){
            if (salteo == Salteo.DIRECTO) return "Plaza C (Directo)";
            return "Via Temperley";
        }

        if (fin == Station.BOSQUES){
            if (salteo == Salteo.DIRECTO) return "Bosques T (Directo)";
            return Ramal.BOSQUES_T.getNombre();
        }

        if (fin == Station.VARELA){
            if (salteo == Salteo.DIRECTO) return "Varela T (Directo)";
            return Ramal.VARELA_T.getNombre();
        }

        return fin.getSimplified();
    }
}
