package cs10.apps.travels.tracer.generator;

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
    MARMOL("Estación José Mármol"),
    CALZADA("Estación Rafael Calzada"),
    CLAYPOLE("Estación Claypole"),
    ARDIGO("Km 26"),
    VARELA("Estación Varela"),
    ZEBALLOS("Estación Zeballos"),
    BOSQUES("Estación Bosques"),
    SOURIGUES("Estación Sourigues"),
    RANELAGH("Estación Ranelagh"),
    VILLA_ESP("Estación Villa España"),
    BERA("Estación Berazategui"),
    EZPELETA("Estación Ezpeleta"),
    QUILMES("Estación Quilmes"),
    BERNAL("Estación Bernal"),
    DON_BOSCO("Estación Don Bosco"),
    WILDE("Estación Wilde"),
    DOMINICO("Estación Villa Domínico"),
    SARANDI("Estación Sarandí"),
    PLATANOS("Estación Plátanos"),
    HUDSON("Estación Hudson"),
    PEREYRA("Estación Pereyra"),
    VILLA_ELISA("Estación Villa Elisa"),
    CITY_BELL("Estación City Bell"),
    GONNET("Estación Gonnet"),
    RINGUELET("Estación Ringuelet"),
    TOLOSA("Estación Tolosa"),
    LA_PLATA("Estación La Plata"),
    ADROGUE("Estación Adrogué"),
    BURZACO("Estación Burzaco"),
    LONGCHAMPS("Estación Longchamps"),
    GLEW("Estación Glew"),
    GUERNICA("Estación Guernica"),
    KORN("Estación Alejandro Korn");

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
}
