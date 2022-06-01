package cs10.apps.travels.tracer.db.filler;

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
    SARANDI("Estación Sarandí");

    private final String nombre;

    Station(String nombre){
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }
}
