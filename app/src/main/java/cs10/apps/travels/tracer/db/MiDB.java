package cs10.apps.travels.tracer.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import cs10.apps.travels.tracer.model.Circuito;
import cs10.apps.travels.tracer.model.Coffee;
import cs10.apps.travels.tracer.model.Comunicacion;
import cs10.apps.travels.tracer.model.Estacion;
import cs10.apps.travels.tracer.model.FormacionCircuito;
import cs10.apps.travels.tracer.model.Horario;
import cs10.apps.travels.tracer.model.Parada;
import cs10.apps.travels.tracer.model.Recarga;
import cs10.apps.travels.tracer.model.Tren;
import cs10.apps.travels.tracer.model.Viaje;
import cs10.apps.travels.tracer.model.Zone;
import cs10.apps.travels.tracer.model.lines.CustomBusLine;
import cs10.apps.travels.tracer.model.prices.TarifaBus;
import cs10.apps.travels.tracer.model.prices.TarifaTren;
import cs10.apps.travels.tracer.model.roca.HorarioTren;
import cs10.apps.travels.tracer.model.roca.ServicioTren;
import cs10.apps.travels.tracer.pages.coffee.db.CoffeeDao;
import cs10.apps.travels.tracer.pages.home.db.ServicioDao;
import cs10.apps.travels.tracer.pages.manage_lines.db.LinesDao;
import cs10.apps.travels.tracer.pages.live.entity.MediumStop;
import cs10.apps.travels.tracer.pages.manage_zones.db.ZonesDao;
import cs10.apps.travels.tracer.pages.month_summary.db.RecargaDao;
import cs10.apps.travels.tracer.pages.registry.db.TravelsDao;
import cs10.apps.travels.tracer.pages.stops.db.ParadasDao;
import cs10.apps.travels.tracer.pages.stops.db.SafeStopsDao;
import cs10.apps.travels.tracer.pages.registry.db.ViajesDao;
import cs10.apps.travels.tracer.pages.stops.db.TrainsDao;

@Database(entities = {Circuito.class, Comunicacion.class, Estacion.class, FormacionCircuito.class,
        Tren.class, Horario.class, Parada.class, Viaje.class, TarifaBus.class, TarifaTren.class,
        Coffee.class, Recarga.class, ServicioTren.class, HorarioTren.class, CustomBusLine.class,
        Zone.class, MediumStop.class
}, version = 23)
public abstract class MiDB extends RoomDatabase {
    private static MiDB instance;

    public static MiDB getInstance(Context context) {
        if (instance == null){
            Migration[] migrations = new Migration[]{
                    TIPO_PARADA_MIGRATION, TARIFA_MIGRATION, SCHEMA_MIGRATION,
                    COSTO_TARIFA_MIGRATION, ADD_FIXED_VIAJES_MIGRATION, TARIFA_BUS_MIGRATION,
                    ADD_COSTO_TO_VIAJE, CREATE_COFFEE_TABLE, CREATE_RECARGA_TABLE,
                    CREATE_ROCA_TABLES, FIX_HORARIOS_TABLE, ADD_RAMAL_COLUMN_TO_SERVICIOS,
                    ADD_WEEK_DAY_COLUMN_TO_TRAVELS, ADD_RATE_COLUMN_TO_TRAVELS, CREATE_LINES_TABLE,
                    CREATE_ZONES_TABLE, CREATE_MEDIUM_STOPS_TABLE, DROP_MEDIUM_STOPS
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

    private static final Migration ADD_WEEK_DAY_COLUMN_TO_TRAVELS = new Migration(17,18) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE Viaje ADD COLUMN wd integer not null default 0");
        }
    };

    private static final Migration ADD_RATE_COLUMN_TO_TRAVELS = new Migration(18,19) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE Viaje ADD COLUMN rate INTEGER");
        }
    };

    private static final Migration CREATE_LINES_TABLE = new Migration(19, 20) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE lines ( " +
                    "id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    "number INTEGER, name TEXT, color INTEGER NOT NULL ) ");
        }
    };

    private static final Migration CREATE_ZONES_TABLE = new Migration(20, 21) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE Zone (id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL, x0 REAL NOT NULL, x1 REAL NOT NULL, " +
                    "y0 REAL NOT NULL, y1 REAL NOT NULL )");
        }
    };

    private static final Migration CREATE_MEDIUM_STOPS_TABLE = new Migration(21, 22) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase supportSQLiteDatabase) {
            supportSQLiteDatabase.execSQL("CREATE TABLE MediumStop (" +
                    "id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    "type INTEGER NOT NULL, line INTEGER, ramal TEXT, " +
                    "prev TEXT NOT NULL, name TEXT NOT NULL, next TEXT NOT NULL, " +
                    "destination TEXT NOT NULL)");
        }
    };

    private static final Migration DROP_MEDIUM_STOPS = new Migration(22, 23) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {

        }
    };

    // DAOs
    public abstract SafeStopsDao safeStopsDao();
    public abstract ParadasDao paradasDao();
    public abstract ViajesDao viajesDao();
    public abstract CoffeeDao coffeeDao();
    public abstract RecargaDao recargaDao();
    public abstract ServicioDao servicioDao();
    public abstract LinesDao linesDao();
    public abstract ZonesDao zonesDao();
    public  abstract TravelsDao travelsDao();
    public abstract TrainsDao trainsDao();
}
