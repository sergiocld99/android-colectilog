package cs10.apps.travels.tracer.data.generator;

import java.util.LinkedList;
import java.util.List;

import cs10.apps.travels.tracer.modules.ZoneData;

public enum Station {
    PLAZA("Plaza Constitución"),
    YRIGOYEN("Estación H. Yrigoyen"),
    AVELLANEDA("Estación Santillán y Kosteki"),
    GERLI("Estación Gerli"),
    LANUS("Estación Lanús"),
    ESCALADA("Estación Remedios de Escalada"),
    BANFIELD("Estación Banfield"),
    LOMAS("Estación Lomas de Zamora"),
    TEMPERLEY("Estación Temperley"),
    ADROGUE("Estación Adrogué"),
    BURZACO("Estación Burzaco"),
    LONGCHAMPS("Estación Longchamps"),
    GLEW("Estación Glew"),
    GUERNICA("Estación Guernica"),
    KORN("Estación Alejandro Korn"),

    MARMOL("Estación José Mármol"),
    CALZADA("Estación Rafael Calzada"),
    CLAYPOLE("Estación Claypole"),
    ARDIGO("Km 26", -34.80, -58.30),
    VARELA("Estación Varela", -34.81, -58.27),
    ZEBALLOS("Estación Zeballos", -34.81, -58.25),
    BOSQUES("Estación Bosques", -34.81, -58.22),

    SARANDI("Estación Sarandí"),
    DOMINICO("Estación Villa Domínico"),
    WILDE("Estación Wilde"),
    DON_BOSCO("Estación Don Bosco"),
    BERNAL("Estación Bernal"),
    QUILMES("Estación Quilmes"),
    EZPELETA("Estación Ezpeleta"),
    BERA("Estación Berazategui"),
    VILLA_ESP("Estación Villa España"),
    RANELAGH("Estación Ranelagh"),
    SOURIGUES("Estación Sourigues"),

    PLATANOS("Estación Plátanos"),
    HUDSON("Estación Hudson", -34.79, -58.15),
    PEREYRA("Estación Pereyra", -34.83, -58.09),
    VILLA_ELISA("Estación Villa Elisa", -34.84, -58.07),
    CITY_BELL("Estación City Bell", -34.86, -58.04),
    GONNET("Estación Gonnet", -34.87, -58.01),
    RINGUELET("Estación Ringuelet", -34.88, -57.99),
    TOLOSA("Estación Tolosa", -34.89, -57.96),
    LA_PLATA("Estación La Plata", -34.90, -57.94),

    ARQUI("Estación Arquitectura", -34.90, -57.94),
    INFO("Estación Informática", -34.90, -57.93),
    MEDICINA("Estación Medicina", -34.90, -57.92),
    PERIODISMO("Estación Periodismo", -34.91, -57.92),
    DIAG_73("Estación Diagonal 73", -34.92, -57.91),
    POLICLINICO("Estación Policlínico", -34.92, -57.92);

    private final String nombre;

    // de poca precisión, para calcular zona
    private final double latitude, longitude;

    Station(String nombre){
        this(nombre, 0.0, 0.0);
    }

    Station(String nombre, double latitude, double longitude){
        this.nombre = nombre;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getNombre() {
        return nombre;
    }

    public String getSimplified(){
        return nombre.replace("Estación","").trim();
    }

    public static Station findByNombre(String target){
        for (Station s : values()){
            if (s.getNombre().equals(target)) return s;
        }

        return null;
    }

    public static List<Station> findStationsAtZone(int xCode, int yCode, int limit){
        List<Station> result = new LinkedList<>();

        for (Station s : values()){
            if (s.latitude == 0) continue;     // evito procesamiento innecesario

            int s_xcode = ZoneData.Companion.getXCode(s.latitude);
            int s_ycode = ZoneData.Companion.getYCode(s.longitude);

            // se permite errar en alguno de los pares hasta en 1 unidad
            if (Math.abs(xCode - s_xcode) + Math.abs(yCode - s_ycode) < 2){
                result.add(s);
                if (result.size() == limit) break;
            }
        }

        return result;
    }
}
