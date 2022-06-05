package cs10.apps.travels.tracer.generator;

import cs10.apps.common.android.CS_Time;
import cs10.apps.travels.tracer.db.MiDB;

public abstract class CircuitoFiller {
    private final CircuitoHelper helper;

    public CircuitoFiller(CircuitoHelper helper){
        this.helper = helper;
    }

    protected void createAux(Station inicio, int hora, int minuto, int cantidad, int frecuencia,
                           Station fin, boolean haciaAdelante, MiDB db){

        CS_Time time = new CS_Time();
        time.setHour(hora);
        time.setMinute(minuto);

        for (int i=0; i<cantidad; i++){
            helper.create(inicio, time.getHour(), time.getMinute(), fin, haciaAdelante, db);
            time.add(frecuencia);
        }
    }

    protected void createAux(Station inicio, String[] salidas, Station fin, boolean haciaAdelante, MiDB db){
        CS_Time time = new CS_Time();

        for (String s : salidas){
            time.setFromString(s);
            helper.create(inicio, time.getHour(), time.getMinute(), fin, haciaAdelante, db);
        }
    }
}
