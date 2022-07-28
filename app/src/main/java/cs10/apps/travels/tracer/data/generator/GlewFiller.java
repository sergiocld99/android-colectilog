package cs10.apps.travels.tracer.data.generator;

import cs10.apps.travels.tracer.db.MiDB;

public class GlewFiller extends AdvancedFiller {

    public GlewFiller(DelayData delayData){
        super(new GlewHelper(delayData));
    }

    public void createIda(MiDB db){
        helper.create(Station.PLAZA, 4,15, Station.KORN, true, db);

        // 13 directos cada 24 min, de 4:34 a 9:22
        // 10 directos cada 24 min, de 16:10 a 19:46
        createAux(Station.PLAZA, 4,34,13,24, Station.KORN, true, Salteo.DIRECTO, db);
        createAux(Station.PLAZA, 16, 10, 10, 24, Station.KORN, true, Salteo.DIRECTO, db);

        // 12 completos cada 24 min, de 4:40 a 9:04
        // luego son 16 de 9:50 hasta 15:50
        // luego son 4 de 16:16 a 17:28
        // luego son 4 de 18:16 a 19:28
        // luego son 4 de 20:14 a 21:26
        createAux(Station.PLAZA, 4, 40, 12, 24, Station.KORN, true, db);
        createAux(Station.PLAZA, 9,50,16,24, Station.KORN, true, db);
        createAux(Station.PLAZA, 16, 16, 4, 24, Station.KORN, true, db);
        createAux(Station.PLAZA, 18,16,4,24, Station.KORN, true, db);
        createAux(Station.PLAZA, 20,14,4,24, Station.KORN, true, db);
        String[] salidas = new String[]{"17:51", "19:51", "21:54", "22:24"};
        createAux(Station.PLAZA, salidas, Station.KORN, true, db);

        // 3 completos hasta burzaco, cada 48 min, de 7:00 a 9:36
        createAux(Station.PLAZA, 7,0,3,48, Station.BURZACO, true, db);
        salidas = new String[]{"16:36", "18:12", "19:08", "21:40", "22:08"};
        createAux(Station.PLAZA, salidas, Station.BURZACO, true, db);

        // servicios hasta glew
        // luego 15 cada 24 min, de 10:02 a 15:38
        // luego 4 cada 24 min, de 20:02 a 21:14
        salidas = new String[]{"9:31", "17:08", "22:40"};
        createAux(Station.PLAZA, salidas, Station.GLEW, true, db);
        createAux(Station.PLAZA, 10,2,15,24, Station.GLEW, true, db);
        createAux(Station.PLAZA, 20,2,4,24, Station.GLEW, true, db);
    }

    public void createVuelta(MiDB db){
        // 6 directos desde korn, de 5:45 a 7:45 cada 24 min - luego 3 de 8:33 a 9:21
        // luego son 4 de 17:21 a 18:33
        createAux(Station.KORN, 5, 45, 6, 24, Station.PLAZA, false, Salteo.DIRECTO, db);
        createAux(Station.KORN, 8, 33, 3, 24, Station.PLAZA, false, Salteo.DIRECTO, db);
        createAux(Station.KORN, 17, 21, 4, 24, Station.PLAZA, false, Salteo.DIRECTO, db);
        String[] salidas = new String[]{"8:08", "18:56"};
        createAux(Station.KORN, salidas, Station.PLAZA, false, Salteo.DIRECTO, db);

        // 15 completos de korn a plaza, de 4:21 a 9:57 cada 24 min, luego 15 de 10:22 hasta 15:58
        // luego hay 4 de 17:33 a 18:45. Después cada 12 min, son 4 de 19:33 a 20:09
        // luego hay 5 de 20:22 a 21:10 (cada 12 min).
        createAux(Station.KORN, 4,21,15,24, Station.PLAZA, false, db);
        createAux(Station.KORN, 10,22,15,24, Station.PLAZA, false, db);
        createAux(Station.KORN, 17,33,4,24, Station.PLAZA, false, db);
        createAux(Station.KORN, 19,33,4,12, Station.PLAZA, false, db);
        createAux(Station.KORN, 20,22,5,12, Station.PLAZA, false, db);
        salidas = new String[]{"9:45", "10:10", "16:21", "16:47", "17:09", "19:08", "19:21", "21:34", "21:58"};
        createAux(Station.KORN, salidas, Station.PLAZA, false, db);

        // ultimo servicio
        helper.create(Station.KORN, 22,46,Station.TEMPERLEY, false, db);

        // desde glew, luego hay 11 de 10:44 a 14:44 cada 24 min, luego hay 3 de 21:32 a 22:20
        createAux(Station.GLEW, 10,44,11,24, Station.PLAZA, false, db);
        createAux(Station.GLEW, 21,32,3,24, Station.PLAZA, false, db);
        salidas = new String[]{"4:08", "15:31", "18:12"};
        createAux(Station.GLEW, salidas, Station.PLAZA, false, db);

        // desde glew, directos
        salidas = new String[]{"5:07", "15:04", "15:50", "16:19", "16:43"};
        createAux(Station.GLEW, salidas, Station.PLAZA, false, Salteo.DIRECTO, db);

        // desde burzaco, directos
        salidas = new String[]{"4:56", "5:41"};
        createAux(Station.BURZACO, salidas, Station.PLAZA, false, Salteo.DIRECTO, db);

        // desde burzaco, completos
        salidas = new String[]{"6:12", "7:01", "7:49", "8:37", "9:24", "17:24", "19:09", "19:57"};
        createAux(Station.BURZACO, salidas, Station.PLAZA, false, db);
    }
}
