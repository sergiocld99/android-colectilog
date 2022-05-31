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

    /*
        Desde las 4:26 hasta las 9:38, parten 14 trenes DIRECTOS desde PLAZA cada 24 min exactos.
        Luego, tenemos 10 servicios NORMALES que arrancan en diferentes horarios (no correlation).
        Después entre 16:02 y 19:38 tenemos 10 trenes DIRECTOS, cada 14 min nuevamente.
        Por último, se tienen 4 servicios NORMALES cada 30 minutos, de 20:04 a 21:34
     */

    public static final int[] PRIMER_DIRECTO_AM = new int[]{4,26};
    public static final int CANT_DIRECTOS_AM = 14;
    public static final int FRECUENCIA_DIRECTO = 24;

    public static final String[] SALIDA_MATUTINOS = new String[]{
            "10:04", "10:34", "11:34", "12:04", "12:34",
            "13:02", "14:04", "14:34", "15:04", "15:33"
    };

    public static final int[] PRIMER_DIRECTO_PM = new int[]{16,2};
    public static final int CANT_DIRECTOS_PM = 10;

    public static final int[] PRIMER_NORMAL_NOCTURNO = new int[]{20,4};
    public static final int CANT_NOCTURNOS = 4;
    public static final int FRECUENCIA_NOCTURNO = 30;

    public void create(MiDB db){
        create(db, PRIMER_DIRECTO_AM, CANT_DIRECTOS_AM, PARADAS_DIRECTO, ARRIBOS_DIRECTO, FRECUENCIA_DIRECTO);
        createNormalesMatutinos(db);
        create(db, PRIMER_DIRECTO_PM, CANT_DIRECTOS_PM, PARADAS_DIRECTO, ARRIBOS_DIRECTO, FRECUENCIA_DIRECTO);
        create(db, PRIMER_NORMAL_NOCTURNO, CANT_NOCTURNOS, PARADAS_NORMAL, ARRIBOS_NORMAL, FRECUENCIA_NOCTURNO);
    }

    public void create(MiDB db, final int[] PRIMER_SERVICIO, final int CANT_SERVICIOS,
                       final String[] PARADAS, final int[] ARRIBOS, final int FRECUENCIA){
        CS_Time salida = new CS_Time();
        salida.setHour(PRIMER_SERVICIO[0]);
        salida.setMinute(PRIMER_SERVICIO[1]);

        for (int d = 0; d<CANT_SERVICIOS; d++){
            ServicioTren servicio = new ServicioTren();
            servicio.setCabecera(PLAZA);
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

    public void createNormalesMatutinos(MiDB db){
        CS_Time salida = new CS_Time();

        for (String salidaNormal : SALIDA_MATUTINOS) {
            salida.setFromString(salidaNormal);

            ServicioTren servicio = new ServicioTren();
            servicio.setCabecera(PLAZA);
            servicio.setTime(salida.getHour(), salida.getMinute());

            // guardar servicio en base de datos, me devuelve el ID adoptado
            long id = db.servicioDao().insert(servicio);

            // ETA de cada estación
            CS_Time arribo = new CS_Time();
            arribo.setHour(salida.getHour());
            arribo.setMinute(salida.getMinute());

            // registrar cada arribo del mismo
            for (int e = 0; e < PARADAS_NORMAL.length; e++) {
                HorarioTren horario = new HorarioTren();
                horario.setService(id);
                horario.setStation(PARADAS_NORMAL[e]);

                // calculate ETA (suponemos que NO hay lapsos mayores a 60' entre 2 estaciones)
                if (e != 0) {
                    int minuteDiff = ARRIBOS_NORMAL[e] - ARRIBOS_NORMAL[e - 1];
                    arribo.addPositive(minuteDiff);     // ej: 1 - 57 = -56' -> 4'
                }

                horario.setHour(arribo.getHour());
                horario.setMinute(arribo.getMinute());

                // guardar horario en base de datos
                db.servicioDao().insertHorario(horario);
            }
        }
    }

}
