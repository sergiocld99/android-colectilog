package cs10.apps.travels.tracer.generator;

import cs10.apps.travels.tracer.db.MiDB;

public class ViaCircuitoFiller extends AdvancedFiller {

    public ViaCircuitoFiller(DelayData delayData){
        super(new ViaCircuitoHelper(delayData));
    }

    // versión usando helper
    public void create2_T(MiDB db){

        // Primer servicio del dia
        helper.create(Station.TEMPERLEY, 4,25, Station.BOSQUES, true, null, db);

        // De 4:26 a 9:38, tenemos 14 trenes DIRECTOS via Temperley > Quilmes cada 24 min
        createAux(Station.PLAZA, 4, 26, 14, 24, Station.PLAZA, true, Salteo.DIRECTO, db);

        // De 4:32 a 9:44, tenemos 14 trenes Bosques T, que paran en todas, cada 24 min
        createAux(Station.PLAZA, 4,32, 14, 24, Station.BOSQUES, true, null, db);

        // De 10:04 a 15:33, vias circuito via Temperley, salteando Gerli
        String[] salidas = new String[]{"10:04", "10:34", "11:34", "12:04", "12:34", "13:02", "14:04", "14:34", "15:04", "15:33"};
        createAux(Station.PLAZA, salidas, Station.PLAZA, true, Salteo.GERLI, db);

        // De 10:20 a 14:49, ramales via Temperley solo hasta varela, salteando Gerli
        salidas = new String[]{"10:20", "11:19", "12:19", "12:49", "13:19", "13:49", "14:49"};
        createAux(Station.PLAZA, salidas, Station.VARELA, true, Salteo.GERLI, db);

        // De 11:05 a 15:51, ramales via Temperley solo hasta Bosques, salteando Gerli
        salidas = new String[]{"11:05", "13:34", "15:17", "15:51"};
        createAux(Station.PLAZA, salidas, Station.BOSQUES, true, Salteo.GERLI, db);

        // De 16:02 a 19:38, tenemos 11 trenes via circuito directos, cada 24 min
        createAux(Station.PLAZA, 16,2, 10, 24, Station.PLAZA,true, Salteo.DIRECTO, db);

        // De 16:08 a 19:44, trenes Bosques T que paran en todas
        salidas = new String[]{"16:08", "16:32", "16:56", "17:44", "18:08", "18:32", "18:56", "19:20", "19:44"};
        createAux(Station.PLAZA, salidas, Station.BOSQUES, true, null, db);

        // De 20:04 a 21:34, 4 trenes via circuito que no paran en Gerli, cada 30 min
        createAux(Station.PLAZA, 20,4, 4, 30, Station.PLAZA, true, Salteo.GERLI, db);

        // De 20:19 a 22:21, trenes Bosques T que no paran en Gerli
        salidas = new String[]{"20:19", "20:49", "21:20", "21:49", "22:21"};
        createAux(Station.PLAZA, salidas, Station.BOSQUES, true, Salteo.GERLI, db);

        // Ultimo servicio
        helper.create(Station.PLAZA, 22,34, Station.VARELA, true, Salteo.GERLI, db);
    }

    // Los servicios que llegan de Plaza solo hasta Bera (via Quilmes), no los registraremos
    public void create2_Q(MiDB db){
        // Primeros servicios
        helper.create(Station.BOSQUES, 4,37, Station.PLAZA, false, null, db);
        helper.create(Station.BERA, 5, 5, Station.PLAZA, false, null, db);
        helper.create(Station.VARELA, 5, 8, Station.PLAZA, false, null, db);

        // De 5:12 a 9:36, hay 12 trenes Bosques T directos a Plaza, cada 24 min
        createAux(Station.BOSQUES, 5,12,12,24, Station.PLAZA, false, Salteo.DIRECTO, db);
        helper.create(Station.BOSQUES, 10, 24, Station.PLAZA, false, Salteo.DIRECTO, db);

        // De 4:54 a 8:54, hay 11 trenes via circuito, que paran en todas, cada 24 min
        createAux(Station.PLAZA, 4, 54, 11, 24, Station.PLAZA, false, null, db);

        // De 9:18 a 14:39, trenes via circuito que saltean Gerli
        String[] salidas = new String[]{"9:18", "9:42", "10:07", "10:41", "11:09", "11:39", "12:09", "12:39", "13:09", "14:09", "14:39"};
        createAux(Station.PLAZA, salidas, Station.PLAZA, false, Salteo.GERLI, db);

        // Servicios de Bosques T, salteando Gerli
        salidas = new String[]{"10:01", "10:47", "12:17", "14:47"};
        createAux(Station.BOSQUES, salidas, Station.PLAZA, false, Salteo.GERLI, db);

        // Servicios de Bosques T que arrancan en Varela, salteando Gerli
        salidas = new String[]{"11:14", "12:54", "13:24", "13:54", "14:24", "15:25"};
        createAux(Station.VARELA, salidas, Station.PLAZA, false, Salteo.GERLI, db);

        // Arrancan los directos de la tarde
        helper.create(Station.PLAZA, 15,7, Station.PLAZA, false, Salteo.DIRECTO, db);
        helper.create(Station.BOSQUES, 16, 14, Station.PLAZA, false, null, db);
        helper.create(Station.VARELA, 16, 32, Station.PLAZA, false, Salteo.DIRECTO, db);
        helper.create(Station.PLAZA, 15, 40, Station.PLAZA, false, null, db);

        // De 16:48 a 18:48 hay 6 trenes directos que salen de bosques a plaza, cada 24 min
        createAux(Station.BOSQUES, 16,48,6,24,Station.PLAZA, false, Salteo.DIRECTO, db);
        helper.create(Station.BOSQUES, 19,36, Station.PLAZA, false, Salteo.DIRECTO, db);

        // De 16:06 a 18:54, hay 8 trenes via circuito, que paran en todas, cada 24 min
        createAux(Station.PLAZA, 16, 6, 8, 24, Station.PLAZA, false, null, db);

        // A las 19:12 parte de bosques un tren que para en todas hasta plaza
        helper.create(Station.BOSQUES, 19,12,Station.PLAZA, false, null, db);

        // De 20:00 a 21:47, trenes que salen de bosques a plaza, y no paran en gerli
        salidas = new String[]{"20:00", "20:22", "20:48", "21:17", "21:47"};
        createAux(Station.BOSQUES, salidas, Station.PLAZA, false, Salteo.GERLI, db);

        // De 19:18 a 20:38, trenes via circuito que no paran en gerli
        salidas = new String[]{"19:18", "19:42", "20:09", "20:38"};
        createAux(Station.PLAZA, salidas, Station.PLAZA, false, Salteo.GERLI, db);

        // Ultimos servicios del dia
        helper.create(Station.PLAZA, 21,9, Station.TEMPERLEY, false, null, db);
        helper.create(Station.PLAZA, 21, 43, Station.TEMPERLEY, false, null, db);
        helper.create(Station.BOSQUES, 22, 17, Station.TEMPERLEY, false, null, db);
        helper.create(Station.BOSQUES, 22, 48, Station.TEMPERLEY, false, null, db);

        // Hay otro tren que pasa por bosques 22:57 pero CREO que es un diesel de gutierrez
    }
}
