package cs10.apps.travels.tracer.db;

import android.content.Context;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import cs10.apps.travels.tracer.model.Circuito;
import cs10.apps.travels.tracer.model.Coffee;
import cs10.apps.travels.tracer.model.Comunicacion;
import cs10.apps.travels.tracer.model.Estacion;
import cs10.apps.travels.tracer.model.FormacionCircuito;
import cs10.apps.travels.tracer.model.Horario;
import cs10.apps.travels.tracer.model.Parada;
import cs10.apps.travels.tracer.model.Recarga;
import cs10.apps.travels.tracer.model.TipoDia;
import cs10.apps.travels.tracer.model.Tren;
import cs10.apps.travels.tracer.model.Viaje;
import cs10.apps.travels.tracer.model.prices.TarifaBus;
import cs10.apps.travels.tracer.model.prices.TarifaTren;
import cs10.apps.travels.tracer.model.roca.HorarioTren;
import cs10.apps.travels.tracer.model.roca.ServicioTren;

@Database(entities = {Circuito.class, Comunicacion.class, Estacion.class, FormacionCircuito.class,
        Tren.class, Horario.class, Parada.class, Viaje.class, TarifaBus.class, TarifaTren.class,
        Coffee.class, Recarga.class, ServicioTren.class, HorarioTren.class}, version = 17)
public abstract class MiDB extends RoomDatabase {
    private static MiDB instance;
    public static final String RAMAL_LP = "Constitución - La Plata";

    public static MiDB getInstance(Context context) {
        if (instance == null){
            Migration[] migrations = new Migration[]{
                    TIPO_PARADA_MIGRATION, TARIFA_MIGRATION, SCHEMA_MIGRATION,
                    COSTO_TARIFA_MIGRATION, ADD_FIXED_VIAJES_MIGRATION, TARIFA_BUS_MIGRATION,
                    ADD_COSTO_TO_VIAJE, CREATE_COFFEE_TABLE, CREATE_RECARGA_TABLE,
                    CREATE_ROCA_TABLES, FIX_HORARIOS_TABLE, ADD_RAMAL_COLUMN_TO_SERVICIOS
            };

            instance = Room.databaseBuilder(context.getApplicationContext(), MiDB.class,
                    "miDb").addMigrations(migrations).build();
        }

        return instance;
    }

    private static final Migration TIPO_PARADA_MIGRATION = new Migration(4,5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE parada ADD COLUMN tipo INTEGER NOT NULL DEFAULT 0");
        }
    };

    private static final Migration TARIFA_MIGRATION = new Migration(5,6) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE Tarifa (" +
                    "line INTEGER NOT NULL DEFAULT 0, " +
                    "inicio TEXT NOT NULL, " +
                    "fin TEXT NOT NULL, " +
                    "PRIMARY KEY(line, inicio, fin))");
        }
    };

    private static final Migration SCHEMA_MIGRATION = new Migration(6,7) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {

        }
    };

    private static final Migration COSTO_TARIFA_MIGRATION = new Migration(7,8) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE Tarifa ADD COLUMN costo REAL DEFAULT 0");
            database.execSQL("INSERT INTO Tarifa (line, inicio, fin) " +
                    "SELECT DISTINCT linea as line, nombrePdaInicio as inicio, nombrePdaFin as fin " +
                    "FROM Viaje v WHERE linea is not null");
        }
    };

    private static final Migration ADD_FIXED_VIAJES_MIGRATION = new Migration(9,10) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE Viaje3(" +
                    "id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                    "day INTEGER NOT NULL, month INTEGER NOT NULL, year INTEGER NOT NULL," +
                    "startHour INTEGER NOT NULL, startMinute INTEGER NOT NULL," +
                    "endHour INTEGER, endMinute INTEGER, " +
                    "tipo INTEGER NOT NULL, linea INTEGER, ramal TEXT, " +
                    "nombrePdaInicio TEXT NOT NULL, nombrePdaFin TEXT NOT NULL," +
                    "FOREIGN KEY(nombrePdaInicio) REFERENCES Parada(nombre)," +
                    "FOREIGN KEY(nombrePdaFin) REFERENCES Parada(nombre))");

            database.execSQL("INSERT INTO Viaje3 SELECT * FROM Viaje v");
            database.execSQL("DROP TABLE Viaje");
            database.execSQL("ALTER TABLE Viaje3 RENAME TO Viaje");
        }
    };

    private static final Migration TARIFA_BUS_MIGRATION = new Migration(10,11) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE TarifaBus (" +
                    "line INTEGER NOT NULL DEFAULT 0, " +
                    "inicio TEXT NOT NULL, fin TEXT NOT NULL, " +
                    "costo REAL NOT NULL DEFAULT 0," +
                    "PRIMARY KEY(line, inicio, fin), " +
                    "FOREIGN KEY(inicio) REFERENCES Parada(nombre), " +
                    "FOREIGN KEY(fin) REFERENCES Parada(nombre))");

            database.execSQL("CREATE TABLE TarifaTren (" +
                    "inicio TEXT NOT NULL, fin TEXT NOT NULL, " +
                    "costo REAL NOT NULL DEFAULT 0, " +
                    "PRIMARY KEY(inicio, fin), " +
                    "FOREIGN KEY(inicio) REFERENCES Parada(nombre), " +
                    "FOREIGN KEY(fin) REFERENCES Parada(nombre))");

            database.execSQL("INSERT INTO TarifaBus (line, inicio, fin) " +
                    "SELECT DISTINCT linea as line, nombrePdaInicio as inicio, nombrePdaFin as fin " +
                    "FROM Viaje v WHERE linea is not null");

            database.execSQL("INSERT INTO TarifaTren (inicio, fin) " +
                    "SELECT DISTINCT nombrePdaInicio as inicio, nombrePdaFin as fin " +
                    "FROM Viaje v WHERE linea is null");

            database.execSQL("DROP TABLE Tarifa");
        }
    };

    private static final Migration ADD_COSTO_TO_VIAJE = new Migration(11,12) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE Viaje ADD COLUMN costo REAL NOT NULL DEFAULT 0");
        }
    };

    private static final Migration CREATE_COFFEE_TABLE = new Migration(12,13) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE Coffee (" +
                    "id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                    "year INTEGER NOT NULL, month INTEGER NOT NULL, day INTEGER NOT NULL," +
                    "hour INTEGER NOT NULL, minute INTEGER NOT NULL, price REAL NOT NULL)");
        }
    };

    private static final Migration CREATE_RECARGA_TABLE = new Migration(13,14) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE Recarga (" +
                    "id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                    "year INTEGER NOT NULL, month INTEGER NOT NULL, day INTEGER NOT NULL," +
                    "mount REAL NOT NULL)");
        }
    };

    private static final Migration CREATE_ROCA_TABLES = new Migration(14,15) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE ServicioTren (" +
                    "id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                    "cabecera TEXT, hora INTEGER NOT NULL, minuto INTEGER NOT NULL)");

            database.execSQL("CREATE TABLE HorarioTren (" +
                    "station TEXT NOT NULL, hour INTEGER NOT NULL, " +
                    "minute INTEGER NOT NULL, service INTEGER NOT NULL, " +
                    "PRIMARY KEY(station, hour, minute), " +
                    "FOREIGN KEY(service) REFERENCES ServicioTren(id))");
        }
    };

    private static final Migration FIX_HORARIOS_TABLE = new Migration(15,16) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("DROP TABLE HorarioTren");

            database.execSQL("CREATE TABLE HorarioTren (" +
                    "service INTEGER NOT NULL, hour INTEGER NOT NULL," +
                    "minute INTEGER NOT NULL, station TEXT, " +
                    "PRIMARY KEY(service, hour, minute), " +
                    "FOREIGN KEY(service) REFERENCES ServicioTren(id))");
        }
    };

    private static final Migration ADD_RAMAL_COLUMN_TO_SERVICIOS = new Migration(16,17) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE ServicioTren ADD COLUMN ramal TEXT");
        }
    };

    public abstract TrenesDao trenesDao();
    public abstract ParadasDao paradasDao();
    public abstract ViajesDao viajesDao();
    public abstract CoffeeDao coffeeDao();
    public abstract RecargaDao recargaDao();
    public abstract ServicioDao servicioDao();

    /*
        Lo que sigue debajo fue una primera implementación fallida para implementar trenes.
        Será eliminado cuando la misma haya sido reemplazada por un modelo consistente.
     */

    public void crearRamalLP(){
        if (trenesDao().getCircuito(RAMAL_LP) != null) return;

        String[] nombres = new String[]{
                "Plaza Constitución", "Santillán y Kosteki", "Sarandí", "Villa Domínico",
                "Wilde", "Don Bosco", "Bernal", "Quilmes", "Ezpeleta", "Berazategui",
                "Plátanos", "Hudson", "Pereyra", "Villa Elisa", "City Bell", "Gonnet",
                "Ringuelet", "Tolosa", "La Plata"
        };

        // creamos todas las estaciones y las guardamos en la BD
        List<Estacion> estaciones = new ArrayList<>(nombres.length);
        for (String nombre : nombres) estaciones.add(new Estacion(nombre));
        for (Estacion e : estaciones) trenesDao().insert(e);

        // creamos el circuito
        Circuito circuito = new Circuito(RAMAL_LP);
        trenesDao().insert(circuito);

        // agregamos las estaciones al circuito
        List<FormacionCircuito> f = new LinkedList<>();
        for (Estacion e : estaciones) f.add(new FormacionCircuito(circuito, e, f.size()));
        for (FormacionCircuito fc : f) trenesDao().insert(fc);

        // unimos las estaciones (esto no afecta al circuito)
        List<Comunicacion> comunicaciones = unirEstaciones(estaciones);
        for (Comunicacion c : comunicaciones) trenesDao().insert(c);
    }

    public void crearHorariosRamalLP(){
        Circuito circuito = trenesDao().getCircuito(RAMAL_LP);
        List<Estacion> estaciones = trenesDao().getAllStations(circuito.id);
        if (trenesDao().getTren(4032) != null) return;
        trenesDao().deleteAllSchedules();
        trenesDao().deleteAllTrains();

        // A La Plata (Lunes a Viernes)
        List<Pair<Integer, String>> data = new LinkedList<>();
        data.add(new Pair<>(4035, "04:38 04:44 04:48 04:51 04:54 04:56 04:59 05:03 05:09 05:13 05:17 05:20 05:27 05:30 05:34 05:38 05:41 05:45 05:48"));
        data.add(new Pair<>(4043, "05:02 05:08 05:12 05:15 05:18 05:20 05:23 05:27 05:33 05:37 05:41 05:44 05:51 05:54 05:58 06:02 06:05 06:09 06:12"));
        data.add(new Pair<>(4051, "05:26 05:32 05:36 05:39 05:42 05:44 05:47 05:51 05:57 06:01 06:05 06:08 06:15 06:18 06:22 06:26 06:29 06:33 06:36"));
        data.add(new Pair<>(4059, "05:50 05:56 06:00 06:03 06:06 06:08 06:11 06:15 06:21 06:25 06:29 06:32 06:39 06:42 06:46 06:50 06:53 06:57 07:00"));
        data.add(new Pair<>(4067, "06:14 06:20 06:24 06:27 06:30 06:32 06:35 06:39 06:45 06:49 06:53 06:56 07:03 07:06 07:10 07:14 07:17 07:21 07:24"));
        data.add(new Pair<>(4075, "06:38 06:44 06:48 06:51 06:54 06:56 06:59 07:03 07:09 07:13 07:17 07:20 07:27 07:30 07:34 07:38 07:41 07:45 07:48"));
        data.add(new Pair<>(4083, "07:02 07:08 07:12 07:15 07:18 07:20 07:23 07:27 07:33 07:37 07:41 07:44 07:51 07:54 07:58 08:02 08:05 08:09 08:12"));
        data.add(new Pair<>(4091, "07:26 07:32 07:36 07:39 07:42 07:44 07:47 07:51 07:57 08:01 08:05 08:08 08:15 08:18 08:22 08:26 08:29 08:33 08:36"));
        data.add(new Pair<>(4099, "07:50 07:56 08:00 08:03 08:06 08:08 08:11 08:15 08:21 08:25 08:29 08:32 08:39 08:42 08:46 08:50 08:53 08:57 09:00"));
        data.add(new Pair<>(4107, "08:14 08:20 08:24 08:27 08:30 08:32 08:35 08:39 08:45 08:49 08:53 08:56 09:03 09:06 09:10 09:14 09:17 09:21 09:24"));
        data.add(new Pair<>(4115, "08:38 08:44 08:48 08:51 08:54 08:56 08:59 09:03 09:09 09:13 09:17 09:20 09:27 09:30 09:34 09:38 09:41 09:45 09:48"));
        data.add(new Pair<>(4123, "09:02 09:08 09:12 09:15 09:18 09:20 09:23 09:27 09:33 09:37 09:41 09:44 09:51 09:54 09:58 10:02 10:05 10:09 10:12"));
        data.add(new Pair<>(4131, "09:26 09:32 09:36 09:39 09:42 09:44 09:47 09:51 09:57 10:01 10:05 10:08 10:15 10:18 10:22 10:26 10:29 10:33 10:36"));
        data.add(new Pair<>(4139, "09:50 09:56 10:00 10:03 10:06 10:08 10:11 10:15 10:21 10:25 10:29 10:32 10:39 10:42 10:46 10:50 10:53 10:57 11:00"));
        data.add(new Pair<>(4147, "10:16 10:22 10:26 10:29 10:32 10:34 10:37 10:41 10:47 10:51 10:55 10:58 11:05 11:08 11:12 11:16 11:19 11:23 11:26"));
        data.add(new Pair<>(4155, "10:48 10:54 10:58 11:01 11:04 11:06 11:09 11:13 11:19 11:23 11:27 11:30 11:37 11:40 11:44 11:48 11:51 11:55 11:58"));
        data.add(new Pair<>(4163, "11:22 11:28 11:32 11:35 11:38 11:40 11:43 11:47 11:53 11:57 12:01 12:04 12:11 12:14 12:18 12:22 12:25 12:29 12:32"));
        data.add(new Pair<>(4171, "11:54 12:00 12:04 12:07 12:10 12:12 12:15 12:19 12:25 12:29 12:33 12:36 12:43 12:46 12:50 12:54 12:57 13:01 13:04"));
        data.add(new Pair<>(4179, "12:24 12:30 12:34 12:37 12:40 12:42 12:45 12:49 12:55 12:59 13:03 13:06 13:13 13:16 13:20 13:24 13:27 13:31 13:34"));
        data.add(new Pair<>(4187, "12:54 13:00 13:04 13:07 13:10 13:12 13:15 13:19 13:25 13:29 13:33 13:36 13:43 13:46 13:50 13:54 13:57 14:01 14:04"));
        data.add(new Pair<>(4195, "13:24 13:30 13:34 13:37 13:40 13:42 13:45 13:49 13:55 13:59 14:03 14:06 14:13 14:16 14:20 14:24 14:27 14:31 14:34"));
        data.add(new Pair<>(4203, "13:54 14:00 14:04 14:07 14:10 14:12 14:15 14:19 14:25 14:29 14:33 14:36 14:43 14:46 14:50 14:54 14:57 15:01 15:04"));
        data.add(new Pair<>(4211, "14:24 14:30 14:34 14:37 14:40 14:42 14:45 14:49 14:55 14:59 15:03 15:06 15:13 15:16 15:20 15:24 15:27 15:31 15:34"));
        data.add(new Pair<>(4219, "14:54 15:00 15:04 15:07 15:10 15:12 15:15 15:19 15:25 15:29 15:33 15:36 15:43 15:46 15:50 15:54 15:57 16:01 16:04"));
        data.add(new Pair<>(4227, "15:24 15:30 15:34 15:37 15:40 15:42 15:45 15:49 15:55 15:59 16:03 16:06 16:13 16:16 16:20 16:24 16:27 16:31 16:34"));
        data.add(new Pair<>(4235, "15:54 16:00 16:04 16:07 16:10 16:12 16:15 16:19 16:25 16:29 16:33 16:36 16:43 16:46 16:50 16:54 16:57 17:01 17:04"));
        data.add(new Pair<>(4243, "16:15 16:21 16:25 16:28 16:31 16:33 16:36 16:40 16:46 16:50 16:54 16:57 17:04 17:07 17:11 17:15 17:18 17:22 17:25"));
        data.add(new Pair<>(4251, "16:38 16:44 16:48 16:51 16:54 16:56 16:59 17:03 17:09 17:13 17:17 17:20 17:27 17:30 17:34 17:38 17:41 17:45 17:48"));
        data.add(new Pair<>(4259, "17:02 17:08 17:12 17:15 17:18 17:20 17:23 17:27 17:33 17:37 17:41 17:44 17:51 17:54 17:58 18:02 18:05 18:09 18:12"));
        data.add(new Pair<>(4267, "17:26 17:32 17:36 17:39 17:42 17:44 17:47 17:51 17:57 18:01 18:05 18:08 18:15 18:18 18:22 18:26 18:29 18:33 18:36"));
        data.add(new Pair<>(4275, "17:50 17:56 18:00 18:03 18:06 18:08 18:11 18:15 18:21 18:25 18:29 18:32 18:39 18:42 18:46 18:50 18:53 18:57 19:00"));
        data.add(new Pair<>(4283, "18:14 18:20 18:24 18:27 18:30 18:32 18:35 18:39 18:45 18:49 18:53 18:56 19:03 19:06 19:10 19:14 19:17 19:21 19:24"));
        data.add(new Pair<>(4291, "18:38 18:44 18:48 18:51 18:54 18:56 18:59 19:03 19:09 19:13 19:17 19:20 19:27 19:30 19:34 19:38 19:41 19:45 19:48"));
        data.add(new Pair<>(4299, "19:02 19:08 19:12 19:15 19:18 19:20 19:23 19:27 19:33 19:37 19:41 19:44 19:51 19:54 19:58 20:02 20:05 20:09 20:12"));
        data.add(new Pair<>(4307, "19:26 19:32 19:36 19:39 19:42 19:44 19:47 19:51 19:57 20:01 20:05 20:08 20:15 20:18 20:22 20:26 20:29 20:33 20:36"));
        data.add(new Pair<>(4315, "19:50 19:56 20:00 20:03 20:06 20:08 20:11 20:15 20:21 20:25 20:29 20:32 20:39 20:42 20:46 20:50 20:53 20:57 21:00"));
        data.add(new Pair<>(4323, "20:23 20:29 20:33 20:36 20:39 20:41 20:44 20:48 20:54 20:58 21:02 21:05 21:12 21:15 21:19 21:23 21:26 21:30 21:33"));
        data.add(new Pair<>(4331, "20:55 21:01 21:05 21:08 21:11 21:13 21:16 21:20 21:26 21:30 21:34 21:37 21:44 21:47 21:51 21:55 21:58 22:02 22:05"));
        data.add(new Pair<>(4339, "21:28 21:34 21:38 21:41 21:44 21:46 21:49 21:53 21:59 22:03 22:07 22:10 22:17 22:20 22:24 22:28 22:31 22:35 22:38"));
        data.add(new Pair<>(4357, "22:17 22:23 22:27 22:30 22:33 22:35 22:38 22:42 22:48 22:52 22:56 22:59 23:06 23:09 23:13 23:17 23:20 23:24 23:27"));
        for (Pair<Integer, String> pair : data) armarHorarios(new Tren(pair.first, circuito), estaciones, pair.second, TipoDia.HABIL);

        // A La Plata - Sábados
        data.clear();
        data.add(new Pair<>(4005, "04:54 05:00 05:04 05:07 05:10 05:12 05:15 05:19 05:25 05:29 05:33 05:36 05:43 05:46 05:50 05:54 05:57 06:01 06:04"));
        data.add(new Pair<>(4013, "05:24 05:30 05:34 05:37 05:40 05:42 05:45 05:49 05:55 05:59 06:03 06:06 06:13 06:16 06:20 06:24 06:27 06:31 06:34"));
        data.add(new Pair<>(4029, "06:20 06:26 06:30 06:33 06:36 06:38 06:41 06:45 06:51 06:55 06:59 07:02 07:09 07:12 07:16 07:20 07:23 07:27 07:30"));
        data.add(new Pair<>(4037, "06:54 07:00 07:04 07:07 07:10 07:12 07:15 07:19 07:25 07:29 07:33 07:36 07:43 07:46 07:50 07:54 07:57 08:01 08:04"));
        data.add(new Pair<>(4045, "07:24 07:30 07:34 07:37 07:40 07:42 07:45 07:49 07:55 07:59 08:03 08:06 08:13 08:16 08:20 08:24 08:27 08:31 08:34"));
        data.add(new Pair<>(4053, "07:54 08:00 08:04 08:07 08:10 08:12 08:15 08:19 08:25 08:29 08:33 08:36 08:43 08:46 08:50 08:54 08:57 09:01 09:04"));
        data.add(new Pair<>(4061, "08:24 08:30 08:34 08:37 08:40 08:42 08:45 08:49 08:55 08:59 09:03 09:06 09:13 09:16 09:20 09:24 09:27 09:31 09:34"));
        data.add(new Pair<>(4069, "08:54 09:00 09:04 09:07 09:10 09:12 09:15 09:19 09:25 09:29 09:33 09:36 09:43 09:46 09:50 09:54 09:57 10:01 10:04"));
        data.add(new Pair<>(4077, "09:24 09:30 09:34 09:37 09:40 09:42 09:45 09:49 09:55 09:59 10:03 10:06 10:13 10:16 10:20 10:24 10:27 10:31 10:34"));
        data.add(new Pair<>(4085, "09:54 10:00 10:04 10:07 10:10 10:12 10:15 10:19 10:25 10:29 10:33 10:36 10:43 10:46 10:50 10:54 10:57 11:01 11:04"));
        data.add(new Pair<>(4093, "10:24 10:30 10:34 10:37 10:40 10:42 10:45 10:49 10:55 10:59 11:03 11:06 11:13 11:16 11:20 11:24 11:27 11:31 11:34"));
        data.add(new Pair<>(4101, "10:54 11:00 11:04 11:07 11:10 11:12 11:15 11:19 11:25 11:29 11:33 11:36 11:43 11:46 11:50 11:54 11:57 12:01 12:04"));
        data.add(new Pair<>(4109, "11:24 11:30 11:34 11:37 11:40 11:42 11:45 11:49 11:55 11:59 12:03 12:06 12:13 12:16 12:20 12:24 12:27 12:31 12:34"));
        data.add(new Pair<>(4117, "11:54 12:00 12:04 12:07 12:10 12:12 12:15 12:19 12:25 12:29 12:33 12:36 12:43 12:46 12:50 12:54 12:57 13:01 13:04"));
        data.add(new Pair<>(4125, "12:24 12:30 12:34 12:37 12:40 12:42 12:45 12:49 12:55 12:59 13:03 13:06 13:13 13:16 13:20 13:24 13:27 13:31 13:34"));
        data.add(new Pair<>(4133, "12:54 13:00 13:04 13:07 13:10 13:12 13:15 13:19 13:25 13:29 13:33 13:36 13:43 13:46 13:50 13:54 13:57 14:01 14:04"));
        data.add(new Pair<>(4141, "13:24 13:30 13:34 13:37 13:40 13:42 13:45 13:49 13:55 13:59 14:03 14:06 14:13 14:16 14:20 14:24 14:27 14:31 14:34"));
        data.add(new Pair<>(4149, "13:57 14:03 14:07 14:10 14:13 14:15 14:18 14:22 14:28 14:32 14:36 14:39 14:46 14:49 14:53 14:57 15:00 15:04 15:07"));
        data.add(new Pair<>(4157, "14:26 14:32 14:36 14:39 14:42 14:44 14:47 14:51 14:57 15:01 15:05 15:08 15:15 15:18 15:22 15:26 15:29 15:33 15:36"));
        data.add(new Pair<>(4165, "14:54 15:00 15:04 15:07 15:10 15:12 15:15 15:19 15:25 15:29 15:33 15:36 15:43 15:46 15:50 15:54 15:57 16:01 16:04"));
        data.add(new Pair<>(4173, "15:24 15:30 15:34 15:37 15:40 15:42 15:45 15:49 15:55 15:59 16:03 16:06 16:13 16:16 16:20 16:24 16:27 16:31 16:34"));
        data.add(new Pair<>(4181, "15:54 16:00 16:04 16:07 16:10 16:12 16:15 16:19 16:25 16:29 16:33 16:36 16:43 16:46 16:50 16:54 16:57 17:01 17:04"));
        data.add(new Pair<>(4189, "16:24 16:30 16:34 16:37 16:40 16:42 16:45 16:49 16:55 16:59 17:03 17:06 17:13 17:16 17:20 17:24 17:27 17:31 17:34"));
        data.add(new Pair<>(4197, "16:54 17:00 17:04 17:07 17:10 17:12 17:15 17:19 17:25 17:29 17:33 17:36 17:43 17:46 17:50 17:54 17:57 18:01 18:04"));
        data.add(new Pair<>(4205, "17:24 17:30 17:34 17:37 17:40 17:42 17:45 17:49 17:55 17:59 18:03 18:06 18:13 18:16 18:20 18:24 18:27 18:31 18:34"));
        data.add(new Pair<>(4213, "17:54 18:00 18:04 18:07 18:10 18:12 18:15 18:19 18:25 18:29 18:33 18:36 18:43 18:46 18:50 18:54 18:57 19:01 19:04"));
        data.add(new Pair<>(4221, "18:24 18:30 18:34 18:37 18:40 18:42 18:45 18:49 18:55 18:59 19:03 19:06 19:13 19:16 19:20 19:24 19:27 19:31 19:34"));
        data.add(new Pair<>(4229, "18:54 19:00 19:04 19:07 19:10 19:12 19:15 19:19 19:25 19:29 19:33 19:36 19:43 19:46 19:50 19:54 19:57 20:01 20:04"));
        data.add(new Pair<>(4237, "19:24 19:30 19:34 19:37 19:40 19:42 19:45 19:49 19:55 19:59 20:03 20:06 20:13 20:16 20:20 20:24 20:27 20:31 20:34"));
        data.add(new Pair<>(4245, "19:54 20:00 20:04 20:07 20:10 20:12 20:15 20:19 20:25 20:29 20:33 20:36 20:43 20:46 20:50 20:54 20:57 21:01 21:04"));
        data.add(new Pair<>(4253, "20:24 20:30 20:34 20:37 20:40 20:42 20:45 20:49 20:55 20:59 21:03 21:06 21:13 21:16 21:20 21:24 21:27 21:31 21:34"));
        data.add(new Pair<>(4261, "20:54 21:00 21:04 21:07 21:10 21:12 21:15 21:19 21:25 21:29 21:33 21:36 21:43 21:46 21:50 21:54 21:57 22:01 22:04"));
        for (Pair<Integer, String> pair : data) armarHorarios(new Tren(pair.first, circuito), estaciones, pair.second, TipoDia.SABADO);

        // ahora trabajaremos con los servicios a Constitución
        Collections.reverse(estaciones);

        // A Constitución - Lunes a Viernes
        data.clear();
        data.add(new Pair<>(4032, "04:23 04:27 04:31 04:34 04:38 04:42 04:45 04:51 04:53 04:59 05:02 05:08 05:11 05:14 05:16 05:19 05:22 05:27 05:33"));
        data.add(new Pair<>(4036, "04:42 04:46 04:50 04:53 04:57 05:01 05:04 05:10 05:12 05:18 05:21 05:27 05:30 05:33 05:35 05:38 05:41 05:46 05:52"));
        data.add(new Pair<>(4044, "05:11 05:15 05:19 05:22 05:26 05:30 05:33 05:39 05:41 05:47 05:50 05:56 05:59 06:02 06:04 06:07 06:10 06:15 06:21"));
        data.add(new Pair<>(4052, "05:35 05:39 05:43 05:46 05:50 05:54 05:57 06:03 06:05 06:11 06:14 06:20 06:23 06:26 06:28 06:31 06:34 06:39 06:45"));
        data.add(new Pair<>(4060, "05:59 06:03 06:07 06:10 06:14 06:18 06:21 06:27 06:29 06:35 06:38 06:44 06:47 06:50 06:52 06:55 06:58 07:03 07:09"));
        data.add(new Pair<>(4068, "06:23 06:27 06:31 06:34 06:38 06:42 06:45 06:51 06:53 06:59 07:02 07:08 07:11 07:14 07:16 07:19 07:22 07:27 07:33"));
        data.add(new Pair<>(4076, "06:47 06:51 06:55 06:58 07:02 07:06 07:09 07:15 07:17 07:23 07:26 07:32 07:35 07:38 07:40 07:43 07:46 07:51 07:57"));
        data.add(new Pair<>(4084, "07:11 07:15 07:19 07:22 07:26 07:30 07:33 07:39 07:41 07:47 07:50 07:56 07:59 08:02 08:04 08:07 08:10 08:15 08:21"));
        data.add(new Pair<>(4092, "07:35 07:39 07:43 07:46 07:50 07:54 07:57 08:03 08:05 08:11 08:14 08:20 08:23 08:26 08:28 08:31 08:34 08:39 08:45"));
        data.add(new Pair<>(4100, "07:59 08:03 08:07 08:10 08:14 08:18 08:21 08:27 08:29 08:35 08:38 08:44 08:47 08:50 08:52 08:55 08:58 09:03 09:09"));
        data.add(new Pair<>(4108, "08:23 08:27 08:31 08:34 08:38 08:42 08:45 08:51 08:53 08:59 09:02 09:08 09:11 09:14 09:16 09:19 09:22 09:27 09:33"));
        data.add(new Pair<>(4116, "08:47 08:51 08:55 08:58 09:02 09:06 09:09 09:15 09:17 09:23 09:26 09:32 09:35 09:38 09:40 09:43 09:46 09:51 09:57"));
        data.add(new Pair<>(4124, "09:11 09:15 09:19 09:22 09:26 09:30 09:33 09:39 09:41 09:47 09:50 09:56 09:59 10:02 10:04 10:07 10:10 10:15 10:21"));
        data.add(new Pair<>(4132, "09:38 09:42 09:46 09:49 09:53 09:57 10:00 10:06 10:08 10:14 10:17 10:23 10:26 10:29 10:31 10:34 10:37 10:42 10:48"));
        data.add(new Pair<>(4140, "09:59 10:03 10:07 10:10 10:14 10:18 10:21 10:27 10:29 10:35 10:38 10:44 10:47 10:50 10:52 10:55 10:58 11:03 11:09"));
        data.add(new Pair<>(4148, "10:22 10:26 10:30 10:33 10:37 10:41 10:44 10:50 10:52 10:58 11:01 11:07 11:10 11:13 11:15 11:18 11:21 11:26 11:32"));
        data.add(new Pair<>(4156, "10:52 10:56 11:00 11:03 11:07 11:11 11:14 11:20 11:22 11:28 11:31 11:37 11:40 11:43 11:45 11:48 11:51 11:56 12:02"));
        data.add(new Pair<>(4164, "11:22 11:26 11:30 11:33 11:37 11:41 11:44 11:50 11:52 11:58 12:01 12:07 12:10 12:13 12:15 12:18 12:21 12:26 12:32"));
        data.add(new Pair<>(4172, "11:52 11:56 12:00 12:03 12:07 12:11 12:14 12:20 12:22 12:28 12:31 12:37 12:40 12:43 12:45 12:48 12:51 12:56 13:02"));
        data.add(new Pair<>(4180, "12:22 12:26 12:30 12:33 12:37 12:41 12:44 12:50 12:52 12:58 13:01 13:07 13:10 13:13 13:15 13:18 13:21 13:26 13:32"));
        data.add(new Pair<>(4188, "12:52 12:56 13:00 13:03 13:07 13:11 13:14 13:20 13:22 13:28 13:31 13:37 13:40 13:43 13:45 13:48 13:51 13:56 14:02"));
        data.add(new Pair<>(4196, "13:22 13:26 13:30 13:33 13:37 13:41 13:44 13:50 13:52 13:58 14:01 14:07 14:10 14:13 14:15 14:18 14:21 14:26 14:32"));
        data.add(new Pair<>(4204, "13:50 13:54 13:58 14:01 14:05 14:09 14:12 14:18 14:20 14:26 14:29 14:35 14:38 14:41 14:43 14:46 14:49 14:54 15:00"));
        data.add(new Pair<>(4212, "14:22 14:26 14:30 14:33 14:37 14:41 14:44 14:50 14:52 14:58 15:01 15:07 15:10 15:13 15:15 15:18 15:21 15:26 15:32"));
        data.add(new Pair<>(4220, "14:50 14:54 14:58 15:01 15:05 15:09 15:12 15:18 15:20 15:26 15:29 15:35 15:38 15:41 15:43 15:46 15:49 15:54 16:00"));
        data.add(new Pair<>(4228, "15:22 15:26 15:30 15:33 15:37 15:41 15:44 15:50 15:52 15:58 16:01 16:07 16:10 16:13 16:15 16:18 16:21 16:26 16:32"));
        data.add(new Pair<>(4236, "15:52 15:56 16:00 16:03 16:07 16:11 16:14 16:20 16:22 16:28 16:31 16:37 16:40 16:43 16:45 16:48 16:51 16:56 17:02"));
        data.add(new Pair<>(4244, "16:23 16:27 16:31 16:34 16:38 16:42 16:45 16:51 16:53 16:59 17:02 17:08 17:11 17:14 17:16 17:19 17:22 17:27 17:33"));
        data.add(new Pair<>(4252, "16:47 16:51 16:55 16:58 17:02 17:06 17:09 17:15 17:17 17:23 17:26 17:32 17:35 17:38 17:40 17:43 17:46 17:51 17:57"));
        data.add(new Pair<>(4260, "17:11 17:15 17:19 17:22 17:26 17:30 17:33 17:39 17:41 17:47 17:50 17:56 17:59 18:02 18:04 18:07 18:10 18:15 18:21"));
        data.add(new Pair<>(4268, "17:35 17:39 17:43 17:46 17:50 17:54 17:57 18:03 18:05 18:11 18:14 18:20 18:23 18:26 18:28 18:31 18:34 18:39 18:45"));
        data.add(new Pair<>(4276, "17:59 18:03 18:07 18:10 18:14 18:18 18:21 18:27 18:29 18:35 18:38 18:44 18:47 18:50 18:52 18:55 18:58 19:03 19:09"));
        data.add(new Pair<>(4284, "18:23 18:27 18:31 18:34 18:38 18:42 18:45 18:51 18:53 18:59 19:02 19:08 19:11 19:14 19:16 19:19 19:22 19:27 19:33"));
        data.add(new Pair<>(4292, "18:47 18:51 18:55 18:58 19:02 19:06 19:09 19:15 19:17 19:23 19:26 19:32 19:35 19:38 19:40 19:43 19:46 19:51 19:57"));
        data.add(new Pair<>(4300, "19:12 19:16 19:20 19:23 19:27 19:31 19:34 19:40 19:42 19:48 19:51 19:57 20:00 20:03 20:05 20:08 20:11 20:16 20:22"));
        data.add(new Pair<>(4308, "19:35 19:39 19:43 19:46 19:50 19:54 19:57 20:03 20:05 20:11 20:14 20:20 20:23 20:26 20:28 20:31 20:34 20:39 20:45"));
        data.add(new Pair<>(4316, "19:58 20:02 20:06 20:09 20:13 20:17 20:20 20:26 20:28 20:34 20:37 20:43 20:46 20:49 20:51 20:54 20:57 21:02 21:08"));
        data.add(new Pair<>(4324, "20:22 20:26 20:30 20:33 20:37 20:41 20:44 20:50 20:52 20:58 21:01 21:07 21:10 21:13 21:15 21:18 21:21 21:26 21:32"));
        data.add(new Pair<>(4348, "21:53 21:57 22:01 22:04 22:08 22:12 22:15 22:21 22:23 22:29 22:32 22:38 22:41 22:44 22:46 22:49 22:52 22:57 23:03"));
        data.add(new Pair<>(4356, "22:17 22:21 22:25 22:28 22:32 22:36 22:39 22:45 22:47 22:53 22:56 23:02 23:05 23:08 23:10 23:13 23:16 23:21 23:27"));
        for (Pair<Integer, String> pair : data) armarHorarios(new Tren(pair.first, circuito), estaciones, pair.second, TipoDia.HABIL);

        // A Constitución - Sábados
        data.clear();
        data.add(new Pair<>(4016, "04:43 04:47 04:51 04:54 04:58 05:02 05:05 05:11 05:13 05:19 05:22 05:28 05:31 05:34 05:36 05:39 05:42 05:47 05:53"));
        data.add(new Pair<>(4024, "05:22 05:26 05:30 05:33 05:37 05:41 05:44 05:50 05:52 05:58 06:01 06:07 06:10 06:13 06:15 06:18 06:21 06:26 06:32"));
        data.add(new Pair<>(4032, "05:55 05:59 06:03 06:06 06:10 06:14 06:17 06:23 06:25 06:31 06:34 06:40 06:43 06:46 06:48 06:51 06:54 06:59 07:05"));
        data.add(new Pair<>(4040, "06:22 06:26 06:30 06:33 06:37 06:41 06:44 06:50 06:52 06:58 07:01 07:07 07:10 07:13 07:15 07:18 07:21 07:26 07:32"));
        data.add(new Pair<>(4048, "06:52 06:56 07:00 07:03 07:07 07:11 07:14 07:20 07:22 07:28 07:31 07:37 07:40 07:43 07:45 07:48 07:51 07:56 08:02"));
        data.add(new Pair<>(4056, "07:22 07:26 07:30 07:33 07:37 07:41 07:44 07:50 07:52 07:58 08:01 08:07 08:10 08:13 08:15 08:18 08:21 08:26 08:32"));
        data.add(new Pair<>(4064, "07:52 07:56 08:00 08:03 08:07 08:11 08:14 08:20 08:22 08:28 08:31 08:37 08:40 08:43 08:45 08:48 08:51 08:56 09:02"));
        data.add(new Pair<>(4072, "08:22 08:26 08:30 08:33 08:37 08:41 08:44 08:50 08:52 08:58 09:01 09:07 09:10 09:13 09:15 09:18 09:21 09:26 09:32"));
        data.add(new Pair<>(4080, "08:52 08:56 09:00 09:03 09:07 09:11 09:14 09:20 09:22 09:28 09:31 09:37 09:40 09:43 09:45 09:48 09:51 09:56 10:02"));
        data.add(new Pair<>(4088, "09:22 09:26 09:30 09:33 09:37 09:41 09:44 09:50 09:52 09:58 10:01 10:07 10:10 10:13 10:15 10:18 10:21 10:26 10:32"));
        data.add(new Pair<>(4096, "09:52 09:56 10:00 10:03 10:07 10:11 10:14 10:20 10:22 10:28 10:31 10:37 10:40 10:43 10:45 10:48 10:51 10:56 11:02"));
        data.add(new Pair<>(4104, "10:22 10:26 10:30 10:33 10:37 10:41 10:44 10:50 10:52 10:58 11:01 11:07 11:10 11:13 11:15 11:18 11:21 11:26 11:32"));
        data.add(new Pair<>(4112, "10:52 10:56 11:00 11:03 11:07 11:11 11:14 11:20 11:22 11:28 11:31 11:37 11:40 11:43 11:45 11:48 11:51 11:56 12:02"));
        data.add(new Pair<>(4120, "11:22 11:26 11:30 11:33 11:37 11:41 11:44 11:50 11:52 11:58 12:01 12:07 12:10 12:13 12:15 12:18 12:21 12:26 12:32"));
        data.add(new Pair<>(4128, "11:52 11:56 12:00 12:03 12:07 12:11 12:14 12:20 12:22 12:28 12:31 12:37 12:40 12:43 12:45 12:48 12:51 12:56 13:02"));
        data.add(new Pair<>(4136, "12:22 12:26 12:30 12:33 12:37 12:41 12:44 12:50 12:52 12:58 13:01 13:07 13:10 13:13 13:15 13:18 13:21 13:26 13:32"));
        data.add(new Pair<>(4144, "12:52 12:56 13:00 13:03 13:07 13:11 13:14 13:20 13:22 13:28 13:31 13:37 13:40 13:43 13:45 13:48 13:51 13:56 14:02"));
        data.add(new Pair<>(4152, "13:22 13:26 13:30 13:33 13:37 13:41 13:44 13:50 13:52 13:58 14:01 14:07 14:10 14:13 14:15 14:18 14:21 14:26 14:32"));
        data.add(new Pair<>(4160, "13:52 13:56 14:00 14:03 14:07 14:11 14:14 14:20 14:22 14:28 14:31 14:37 14:40 14:43 14:45 14:48 14:51 14:56 15:02"));
        data.add(new Pair<>(4168, "14:22 14:26 14:30 14:33 14:37 14:41 14:44 14:50 14:52 14:58 15:01 15:07 15:10 15:13 15:15 15:18 15:21 15:26 15:32"));
        data.add(new Pair<>(4176, "14:52 14:56 15:00 15:03 15:07 15:11 15:14 15:20 15:22 15:28 15:31 15:37 15:40 15:43 15:45 15:48 15:51 15:56 16:02"));
        data.add(new Pair<>(4184, "15:22 15:26 15:30 15:33 15:37 15:41 15:44 15:50 15:52 15:58 16:01 16:07 16:10 16:13 16:15 16:18 16:21 16:26 16:32"));
        data.add(new Pair<>(4192, "15:52 15:56 16:00 16:03 16:07 16:11 16:14 16:20 16:22 16:28 16:31 16:37 16:40 16:43 16:45 16:48 16:51 16:56 17:02"));
        data.add(new Pair<>(4200, "16:22 16:26 16:30 16:33 16:37 16:41 16:44 16:50 16:52 16:58 17:01 17:07 17:10 17:13 17:15 17:18 17:21 17:26 17:32"));
        data.add(new Pair<>(4208, "16:52 16:56 17:00 17:03 17:07 17:11 17:14 17:20 17:22 17:28 17:31 17:37 17:40 17:43 17:45 17:48 17:51 17:56 18:02"));
        data.add(new Pair<>(4216, "17:22 17:26 17:30 17:33 17:37 17:41 17:44 17:50 17:52 17:58 18:01 18:07 18:10 18:13 18:15 18:18 18:21 18:26 18:32"));
        data.add(new Pair<>(4224, "17:52 17:56 18:00 18:03 18:07 18:11 18:14 18:20 18:22 18:28 18:31 18:37 18:40 18:43 18:45 18:48 18:51 18:56 19:02"));
        data.add(new Pair<>(4232, "18:22 18:26 18:30 18:33 18:37 18:41 18:44 18:50 18:52 18:58 19:01 19:07 19:10 19:13 19:15 19:18 19:21 19:26 19:32"));
        data.add(new Pair<>(4240, "18:55 18:59 19:03 19:06 19:10 19:14 19:17 19:23 19:25 19:31 19:34 19:40 19:43 19:46 19:48 19:51 19:54 19:59 20:05"));
        data.add(new Pair<>(4248, "19:22 19:26 19:30 19:33 19:37 19:41 19:44 19:50 19:52 19:58 20:01 20:07 20:10 20:13 20:15 20:18 20:21 20:26 20:32"));
        data.add(new Pair<>(4256, "19:52 19:56 20:00 20:03 20:07 20:11 20:14 20:20 20:22 20:28 20:31 20:37 20:40 20:43 20:45 20:48 20:51 20:56 21:02"));
        data.add(new Pair<>(4272, "20:52 20:56 21:00 21:03 21:07 21:11 21:14 21:20 21:22 21:28 21:31 21:37 21:40 21:43 21:45 21:48 21:51 21:56 22:02"));
        for (Pair<Integer, String> pair : data) armarHorarios(new Tren(pair.first, circuito), estaciones, pair.second, TipoDia.SABADO);
    }

    private void armarHorarios(@NonNull Tren tren, @NonNull List<Estacion> estaciones, @NonNull String tiempos, @NonNull TipoDia tipoDia){
        List<Horario> resultado = new LinkedList<>();
        String[] tiemposArr = tiempos.split(" ");

        for (int i=0; i<estaciones.size(); i++){
            Estacion e = estaciones.get(i);
            String[] valores = tiemposArr[i].split(":");
            int hora = Integer.parseInt(valores[0]);
            int minuto = Integer.parseInt(valores[1]);
            resultado.add(new Horario(tren, e, tipoDia, hora, minuto));
        }

        for (Horario h : resultado) trenesDao().insert(h);
        trenesDao().insert(tren);
    }

    @NonNull
    private List<Comunicacion> unirEstaciones(@NonNull List<Estacion> estaciones){
        List<Comunicacion> comunicaciones = new LinkedList<>();
        comunicaciones.add(unirPrimera(estaciones));

        for (int i=1; i<estaciones.size()-1; i++){
            Estacion actual = estaciones.get(i);
            Estacion anterior = estaciones.get(i-1);
            Estacion siguiente = estaciones.get(i+1);
            comunicaciones.add(new Comunicacion(actual, anterior));
            comunicaciones.add(new Comunicacion(actual, siguiente));
        }

        comunicaciones.add(unirUltima(estaciones));
        return comunicaciones;
    }

    @NonNull
    private Comunicacion unirPrimera(@NonNull List<Estacion> estaciones){
        return new Comunicacion(estaciones.get(0), estaciones.get(1));
    }

    @NonNull
    private Comunicacion unirUltima(@NonNull List<Estacion> estaciones){
        Estacion ultima = estaciones.get(estaciones.size()-1);
        Estacion anteultima = estaciones.get(estaciones.size()-2);
        return new Comunicacion(ultima, anteultima);
    }
}
