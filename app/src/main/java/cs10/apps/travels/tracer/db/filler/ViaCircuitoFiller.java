package cs10.apps.travels.tracer.db.filler;

import cs10.apps.common.android.CS_Time;
import cs10.apps.travels.tracer.db.MiDB;
import cs10.apps.travels.tracer.model.roca.HorarioTren;
import cs10.apps.travels.tracer.model.roca.ServicioTren;

/*
    Nombres estandarizados de estaciones. En algunos casos se dejan otros nombres no oficiales
    debido a que ya se encontraban previamente cargados en la app (para no sobreescribirlos).

    Versión 1: sólo se cargan los trenes sentido Plaza - Temperley - Quilmes - Plaza.
 */

public class ViaCircuitoFiller {
    public static final String PLAZA = "Plaza Constitución";
    public static final String LANUS = "Estación Lanús";
    public static final String ESCALADA = "Estación Remedios de Escalada";
    public static final String BANFIELD = "Estación Banfield";
    public static final String LOMAS = "Estación Lomas de Zamora";
    public static final String TEMPERLEY = "Estación Temperley";
    public static final String MARMOL = "Estación José Mármol";
    public static final String CALZADA = "Estación Rafael Calzada";
    public static final String CLAYPOLE = "Estación Claypole";
    public static final String ARDIGO = "Km 26";
    public static final String VARELA = "Estación Varela";
    public static final String ZEBALLOS = "Estación Zeballos";
    public static final String BOSQUES = "Estación Bosques";
    public static final String SOURIGUES = "Estación Sourigues";
    public static final String RANELAGH = "Estación Ranelagh";
    public static final String V_ESPANA = "Estación Villa España";
    public static final String BERA = "Estación Berazategui";
    public static final String EZPELETA = "Estación Ezpeleta";
    public static final String QUILMES = "Estación Quilmes";
    public static final String BERNAL = "Estación Bernal";
    public static final String DON_BOSCO = "Estación Don Bosco";
    public static final String WILDE = "Estación Wilde";
    public static final String DOMINICO = "Estación Villa Domínico";
    public static final String SARANDI = "Estación Sarandí";
    public static final String AVELLANEDA = "Estación Santillán y Kosteki";
    public static final String YRIGOYEN = "Estación H. Yrigoyen";
    public static final String GERLI = "Estación Gerli";

    public static final String SERVICIO_IDA = "Via Temperley > Quilmes";
    public static final String SERVICIO_VUELTA = "Via Quilmes > Temperley";
    public static final String SERVICIO_BOSQUES_T = "Bosques T";
    public static final String SERVICIO_DIRECTO = "Bosques T (Directo)";

    public static final String[] PARADAS_DIRECTO = {
            PLAZA, LANUS, LOMAS, MARMOL, CALZADA, CLAYPOLE, ARDIGO, VARELA, ZEBALLOS, BOSQUES,
            SOURIGUES, RANELAGH, V_ESPANA, BERA, EZPELETA, QUILMES, BERNAL, DON_BOSCO,
            WILDE, DOMINICO, SARANDI, AVELLANEDA, PLAZA
    };

    public static final String[] PARADAS_NORMAL = {
            PLAZA, AVELLANEDA, LANUS, ESCALADA, BANFIELD, LOMAS, TEMPERLEY,
            MARMOL, CALZADA, CLAYPOLE, ARDIGO, VARELA, ZEBALLOS, BOSQUES,
            SOURIGUES, RANELAGH, V_ESPANA, BERA, EZPELETA, QUILMES, BERNAL, DON_BOSCO,
            WILDE, DOMINICO, SARANDI, AVELLANEDA, PLAZA
    };

    public static final String[] PARADAS_VUELTA_COMPLETO = {
            PLAZA, AVELLANEDA, SARANDI, DOMINICO, WILDE, DON_BOSCO, BERNAL, QUILMES, EZPELETA, BERA, V_ESPANA, RANELAGH, SOURIGUES, BOSQUES,
            ZEBALLOS, VARELA, ARDIGO, CLAYPOLE, CALZADA, MARMOL, TEMPERLEY, LOMAS, BANFIELD, ESCALADA, LANUS, GERLI, AVELLANEDA, YRIGOYEN, PLAZA
    };

    public static final String[] PARADAS_BOSQUES_T_COMPLETO = {
            PLAZA, YRIGOYEN, AVELLANEDA, GERLI, LANUS, ESCALADA, BANFIELD, LOMAS, TEMPERLEY,
            MARMOL, CALZADA, CLAYPOLE, ARDIGO, VARELA, ZEBALLOS, BOSQUES
    };

    public static final String[] PARADAS_BOSQUES_T_DIRECTO = {
            PLAZA, LANUS, LOMAS, MARMOL, CALZADA, CLAYPOLE, ARDIGO, VARELA, ZEBALLOS, BOSQUES
    };

    public static final int[] ARRIBOS_DIRECTO = {
            26, 37, 45, 51, 54, 57, 1, 6, 9, 13,
            20, 24, 28, 32, 35, 41, 44, 47,
            49, 52, 55, 0, 6
    };

    public static final int[] ARRIBOS_NORMAL = {
            4, 9, 14, 16, 19, 24, 29,
            32, 35, 38, 42, 47, 50, 54,
            0, 4, 8, 13, 16, 22, 25, 28,
            30, 33, 36, 41, 47
    };

    public static final int[] ARRIBOS_VUELTA_COMPLETO = {
            54, 0, 4, 7, 10, 12, 15, 19, 25, 29, 33, 37, 41, 46,
            53, 56, 0, 5, 8, 11, 16, 19, 21, 24, 27, 31, 35, 38, 46
    };

    public static final int[] ARRIBOS_BOSQUES_T_COMPLETO = {
            32, 37, 40, 43, 47, 50, 53, 57, 1,
            4, 7, 10, 14, 19, 22, 26
    };

    /*
        Desde las 4:26 hasta las 9:38, parten 14 trenes DIRECTOS desde PLAZA cada 24 min exactos.
        Luego, tenemos 10 servicios NORMALES que arrancan en diferentes horarios (no correlation).
        Después entre 16:02 y 19:38 tenemos 10 trenes DIRECTOS, cada 14 min nuevamente.
        Por último, se tienen 4 servicios NORMALES cada 30 minutos, de 20:04 a 21:34.

        Para los servicios del Via Circuito en sentido opuesto (Quilmes > Temperley),
        registraremos aquellos que transitan por TODAS las estaciones (COMPLETO).
        En la madrugada, transitan 11 COMPLETOS cada 24 minutos, desde las 4:54 hasta las 8:54.
        Luego hay un COMPLETO que pasa por Bosques a las 16:32 (sale 15:40 de Plaza), y
        posterior a ese hay 8 COMPLETOS cada 24 minutos, desde las 16:06 hasta las 18:54
     */

    public static final int FRECUENCIA_DIRECTO = 24;
    public static final int FRECUENCIA_NOCTURNO = 30;

    public static final String[] SALIDA_MATUTINOS = new String[]{
            "10:04", "10:34", "11:34", "12:04", "12:34",
            "13:02", "14:04", "14:34", "15:04", "15:33"
    };

    /*
        Respecto a los servicios que van solo hasta Bosques, via Temperley, completaremos
        aquellos que transitan también por Yrigoyen y Gerli (madrugada y vespertinos).

        Y de los que salen de Bosques hacia Plaza registramos los directos. Los horarios
        entre estaciones ya se conocen. Son 12 trenes cada 24 minutos, de 5:12 a 9:36
     */

    public static final String[] SALIDA_BOSQUES_T = new String[]{
            "4:32", "4:56", "5:20", "5:44", "6:08", "6:32", "6:56",
            "7:20", "7:44", "8:08", "8:32", "8:56", "9:20", "9:44",
            "16:08", "16:32", "16:56", "17:44",
            "18:08", "18:32", "18:56", "19:20", "19:44"
    };

    private final ViaCircuitoHelper helper;

    public ViaCircuitoFiller(){
        helper = new ViaCircuitoHelper();
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

    private void createAux(Station inicio, int hora, int minuto, int cantidad, int frecuencia,
                           Station fin, boolean haciaAdelante, Salteo salteo, MiDB db){

        CS_Time time = new CS_Time();
        time.setHour(hora);
        time.setMinute(minuto);

        for (int i=0; i<cantidad; i++){
            helper.create(inicio, time.getHour(), time.getMinute(), fin, haciaAdelante, salteo, db);
            time.add(frecuencia);
        }
    }

    private void createAux(Station inicio, String[] salidas, Station fin, boolean haciaAdelante, Salteo salteo, MiDB db){
        CS_Time time = new CS_Time();

        for (String s : salidas){
            time.setFromString(s);
            helper.create(inicio, time.getHour(), time.getMinute(), fin, haciaAdelante, salteo, db);
        }
    }

    // versión clásica
    public void create(MiDB db){
        // Via Temperley > Quilmes
        create(db, 4,26, SERVICIO_IDA, 14, PARADAS_DIRECTO, ARRIBOS_DIRECTO, FRECUENCIA_DIRECTO);
        create(db, SALIDA_MATUTINOS, SERVICIO_IDA, PARADAS_NORMAL, ARRIBOS_NORMAL);
        create(db, 16,2, SERVICIO_IDA, 10, PARADAS_DIRECTO, ARRIBOS_DIRECTO, FRECUENCIA_DIRECTO);
        create(db, 20,4, SERVICIO_IDA, 4, PARADAS_NORMAL, ARRIBOS_NORMAL, FRECUENCIA_NOCTURNO);

        // Via Quilmes > Temperley
        create(db, 4,54, SERVICIO_VUELTA, 11, PARADAS_VUELTA_COMPLETO, ARRIBOS_VUELTA_COMPLETO, FRECUENCIA_DIRECTO);
        create(db, 15,40, SERVICIO_VUELTA, 1, PARADAS_VUELTA_COMPLETO, ARRIBOS_VUELTA_COMPLETO, 0);
        create(db, 16,6, SERVICIO_VUELTA, 8, PARADAS_VUELTA_COMPLETO, ARRIBOS_VUELTA_COMPLETO, FRECUENCIA_DIRECTO);

        // Bosques T
        create(db, SALIDA_BOSQUES_T, SERVICIO_BOSQUES_T, PARADAS_BOSQUES_T_COMPLETO, ARRIBOS_BOSQUES_T_COMPLETO);
        createReverse(db, 5,12, SERVICIO_DIRECTO, 12, PARADAS_BOSQUES_T_DIRECTO, ARRIBOS_DIRECTO, FRECUENCIA_DIRECTO);
        createReverse(db, 16,48, SERVICIO_DIRECTO, 6, PARADAS_BOSQUES_T_DIRECTO, ARRIBOS_DIRECTO, FRECUENCIA_DIRECTO);
        createReverse(db, 19,36, SERVICIO_DIRECTO, 1, PARADAS_BOSQUES_T_DIRECTO, ARRIBOS_DIRECTO, 0);
    }

    public void create(MiDB db, final int START_HOUR, final int START_MINUTE, final String RAMAL, final int CANT_SERVICIOS,
                       final String[] PARADAS, final int[] ARRIBOS, final int FRECUENCIA){
        CS_Time salida = new CS_Time();
        salida.setHour(START_HOUR);
        salida.setMinute(START_MINUTE);

        for (int d = 0; d<CANT_SERVICIOS; d++){
            ServicioTren servicio = new ServicioTren();
            servicio.setCabecera(PARADAS[0]);
            servicio.setRamal(RAMAL);
            servicio.setTime(salida.getHour(), salida.getMinute());

            // guardar servicio en base de datos, me devuelve el ID adoptado
            long id = db.servicioDao().insert(servicio);

            // ETA de cada estación
            CS_Time arribo = new CS_Time();
            arribo.setHour(salida.getHour());
            arribo.setMinute(salida.getMinute());

            // registrar cada arribo del mismo
            for (int e=0; e<PARADAS.length; e++){
                HorarioTren horario = new HorarioTren();
                horario.setService(id);
                horario.setStation(PARADAS[e]);

                // calculate ETA (suponemos que NO hay lapsos mayores a 60' entre 2 estaciones)
                if (e != 0){
                    int minuteDiff = ARRIBOS[e] - ARRIBOS[e-1];
                    arribo.addPositive(minuteDiff);     // ej: 1 - 57 = -56' -> 4'
                }

                horario.setHour(arribo.getHour());
                horario.setMinute(arribo.getMinute());

                // guardar horario en base de datos
                db.servicioDao().insertHorario(horario);
            }

            // actualizar hora de salida para proximo servicio
            salida.add(FRECUENCIA);
        }
    }

    public void create(MiDB db, final String[] SALIDAS, final String RAMAL,
                       final String[] PARADAS, final int[] ARRIBOS){
        CS_Time salida = new CS_Time();

        for (String salidaNormal : SALIDAS) {
            salida.setFromString(salidaNormal);

            ServicioTren servicio = new ServicioTren();
            servicio.setCabecera(PARADAS[0]);
            servicio.setRamal(RAMAL);
            servicio.setTime(salida.getHour(), salida.getMinute());

            // guardar servicio en base de datos, me devuelve el ID adoptado
            long id = db.servicioDao().insert(servicio);

            // ETA de cada estación
            CS_Time arribo = new CS_Time();
            arribo.setHour(salida.getHour());
            arribo.setMinute(salida.getMinute());

            // registrar cada arribo del mismo
            for (int e = 0; e < PARADAS.length; e++) {
                HorarioTren horario = new HorarioTren();
                horario.setService(id);
                horario.setStation(PARADAS[e]);

                // calculate ETA (suponemos que NO hay lapsos mayores a 60' entre 2 estaciones)
                if (e != 0) {
                    int minuteDiff = ARRIBOS[e] - ARRIBOS[e - 1];
                    arribo.addPositive(minuteDiff);     // ej: 1 - 57 = -56' -> 4'
                }

                horario.setHour(arribo.getHour());
                horario.setMinute(arribo.getMinute());

                // guardar horario en base de datos
                db.servicioDao().insertHorario(horario);
            }
        }
    }

    public void createReverse(MiDB db, final int START_HOUR, final int START_MINUTE, final String RAMAL, final int CANT_SERVICIOS,
                       final String[] PARADAS, final int[] ARRIBOS, final int FRECUENCIA){
        CS_Time salida = new CS_Time();
        salida.setHour(START_HOUR);
        salida.setMinute(START_MINUTE);

        for (int d = 0; d<CANT_SERVICIOS; d++){
            ServicioTren servicio = new ServicioTren();
            servicio.setCabecera(PARADAS[PARADAS.length-1]);
            servicio.setRamal(RAMAL);
            servicio.setTime(salida.getHour(), salida.getMinute());

            // guardar servicio en base de datos, me devuelve el ID adoptado
            long id = db.servicioDao().insert(servicio);

            // ETA de cada estación
            CS_Time arribo = new CS_Time();
            arribo.setHour(salida.getHour());
            arribo.setMinute(salida.getMinute());

            // registrar cada arribo del mismo
            for (int e=PARADAS.length-1; e>=0; e--){
                HorarioTren horario = new HorarioTren();
                horario.setService(id);
                horario.setStation(PARADAS[e]);

                // calculate ETA (suponemos que NO hay lapsos mayores a 60' entre 2 estaciones)
                if (e != PARADAS.length-1){
                    int minuteDiff = ARRIBOS[e+1] - ARRIBOS[e];
                    arribo.addPositive(minuteDiff);     // ej: 1 - 57 = -56' -> 4'
                }

                horario.setHour(arribo.getHour());
                horario.setMinute(arribo.getMinute());

                // guardar horario en base de datos
                db.servicioDao().insertHorario(horario);
            }

            // actualizar hora de salida para proximo servicio
            salida.add(FRECUENCIA);
        }
    }

}
