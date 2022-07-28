package cs10.apps.travels.tracer.data.generator;

import cs10.apps.travels.tracer.db.MiDB;

public class LaPlataFiller extends CircuitoFiller {

    public LaPlataFiller(DelayData delayData){
        super(new LaPlataHelper(delayData));
    }

    public void createIda(MiDB db){
        // 14 servicios hasta las 09:50 (11:00 en La Plata)
        // 9 servicios cada 30 min, desde 11:54 a 15:54 (17:04 en La Plata)
        // 9 servicios cada 24 min, desde 16:38 a 19:50 (21:00 en La Plata)
        createAux(Station.PLAZA, 4,38, 14,24, Station.LA_PLATA, true, db);
        createAux(Station.PLAZA, 11, 54, 9, 30, Station.LA_PLATA, true, db);
        createAux(Station.PLAZA, 16, 38, 9, 24, Station.LA_PLATA, true, db);

        // trenes a la plata que no respetan frecuencia
        String[] salidas = new String[]{"10:16", "10:48", "11:22", "16:15", "20:23", "20:55", "21:28", "22:17"};
        createAux(Station.PLAZA, salidas, Station.LA_PLATA, true, db);
    }

    public void createVuelta(MiDB db){
        // de 5:11 a 9:11, 11 servicios cada 24 min
        // de 10:22 a 13:22, 7 servicios cada 30 min
        // de 16:23 a 18:47, 7 servicios cada 24 min
        createAux(Station.LA_PLATA, 5,11, 11, 24, Station.PLAZA, false, db);
        createAux(Station.LA_PLATA, 10, 22, 7, 30, Station.PLAZA, false, db);
        createAux(Station.LA_PLATA, 16, 23, 7, 24, Station.PLAZA, false, db);

        // trenes a plaza que no respetan frecuencia
        String[] salidas = new String[]{"4:23", "4:42", "9:38", "9:59", "13:50", "14:22", "14:50", "15:22", "15:52",
                "19:12", "19:35", "19:58", "20:22", "20:47", "21:53", "22:17"};
        createAux(Station.LA_PLATA, salidas, Station.PLAZA, false, db);
    }
}
