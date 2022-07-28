package cs10.apps.travels.tracer.data.generator;

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
    ARDIGO("Km 26"),
    VARELA("Estación Varela"),
    ZEBALLOS("Estación Zeballos"),
    BOSQUES("Estación Bosques"),

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
    HUDSON("Estación Hudson"),
    PEREYRA("Estación Pereyra"),
    VILLA_ELISA("Estación Villa Elisa"),
    CITY_BELL("Estación City Bell"),
    GONNET("Estación Gonnet"),
    RINGUELET("Estación Ringuelet"),
    TOLOSA("Estación Tolosa"),
    LA_PLATA("Estación La Plata");

    private final String nombre;

    Station(String nombre){
        this.nombre = nombre;
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
}
