package cs10.apps.travels.tracer.data.generator;

public class GlewHelper extends AdvancedHelper {

    private static final Station[] LISTADO_PARADAS = {
            Station.PLAZA, Station.YRIGOYEN, Station.AVELLANEDA, Station.GERLI, Station.LANUS,
            Station.ESCALADA, Station.BANFIELD, Station.LOMAS, Station.TEMPERLEY, Station.ADROGUE,
            Station.BURZACO, Station.LONGCHAMPS, Station.GLEW, Station.GUERNICA, Station.KORN
    };

    private static final Station[] PARADAS_SALTEADAS_DIRECTO = {
            Station.YRIGOYEN, Station.AVELLANEDA, Station.GERLI, Station.LANUS,
            Station.ESCALADA, Station.BANFIELD, Station.LOMAS, Station.TEMPERLEY
    };

    public GlewHelper(DelayData delayData) {
        super(delayData, LISTADO_PARADAS, PARADAS_SALTEADAS_DIRECTO);
    }

    @Override
    protected String getRamalFor(Station inicio, Station fin, boolean haciaAdelante, Salteo salteo) {
        if (salteo == Salteo.DIRECTO){
            if (fin == Station.PLAZA) return "Plaza C (Directo)";
            return fin.getSimplified() + " (Directo)";
        } else return fin.getSimplified();
    }
}
