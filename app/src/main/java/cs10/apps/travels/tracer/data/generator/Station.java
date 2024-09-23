package cs10.apps.travels.tracer.data.generator;

import android.util.Pair;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public enum Station {
    PLAZA("Plaza Constitución", -34.62, -58.38),
    YRIGOYEN("Estación H. Yrigoyen", -34.65, -58.37),
    AVELLANEDA("Estación Santillán y Kosteki", -34.66, -58.37),
    GERLI("Estación Gerli", -34.68, -58.38),
    LANUS("Estación Lanús", -34.70, -58.39),
    ESCALADA("Estación Remedios de Escalada", -34.72, -58.39),
    BANFIELD("Estación Banfield", -34.74, -58.39),
    LOMAS("Estación Lomas de Zamora", -34.76, -58.39),
    TEMPERLEY("Estación Temperley", -34.77, -58.39),
    ADROGUE("Estación Adrogué", -34.79, -58.39),
    BURZACO("Estación Burzaco", -34.82, -58.39),
    LONGCHAMPS("Estación Longchamps", -34.85, -58.38),
    GLEW("Estación Glew", -34.88, -58.38),
    GUERNICA("Estación Guernica", -34.91, -58.38),
    KORN("Estación Alejandro Korn", -34.98, -58.37),

    MARMOL("Estación José Mármol", -34.79, -58.38),
    CALZADA("Estación Rafael Calzada", -34.79, -58.35),
    CLAYPOLE("Estación Claypole", -34.80, -58.33),
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
    BERA("Estación Berazategui", -34.76, -58.20),
    VILLA_ESP("Estación Villa España", -34.77, -58.19),
    RANELAGH("Estación Ranelagh", -34.78, -58.20),
    SOURIGUES("Estación Sourigues", -34.80, -58.21),

    PLATANOS("Estación Plátanos", -34.78, -58.17),
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

    public static List<Station> findStationsAtZone(int xCode, int yCode, int limit, int areaSize){
        List<Station> result = new LinkedList<>();
        List<CandidateStation> candidates = new LinkedList<>();

        for (Station s : values()){
            if (s.latitude == 0) continue;     // evito procesamiento innecesario
            int diffX, diffY;

            if ((diffX = Math.abs(ZoneData.Companion.getXCode(s.latitude) - xCode)) > areaSize) continue;
            if ((diffY = Math.abs(ZoneData.Companion.getYCode(s.longitude) - yCode)) > areaSize) continue;

            candidates.add(new CandidateStation(s, diffX + diffY));
        }

        // sort candidates by "distance"
        Collections.sort(candidates);

        // pick best candidates (first ones)
        for (int i=0; i<Math.min(candidates.size(), limit); i++){
            result.add(candidates.get(i).first);
        }

        return result;
    }

    private static class CandidateStation extends Pair<Station, Integer> implements Comparable<CandidateStation> {

        public CandidateStation(Station first, Integer second) {
            super(first, second);
        }

        @Override
        public int compareTo(CandidateStation candidateStation) {
            return Integer.compare(this.second, candidateStation.second);
        }
    }
}
