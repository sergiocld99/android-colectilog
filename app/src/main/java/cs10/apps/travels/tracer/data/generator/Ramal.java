package cs10.apps.travels.tracer.data.generator;

public enum Ramal {
    BOSQUES_T_QUILMES("Bosques T > Quilmes"),
    BOSQUES_Q_TEMPERLEY("Bosques Q > Temperley"),
    BOSQUES_T("Bosques T"),
    VARELA_T("Varela T");

    private final String nombre;

    Ramal(String nombre){
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }
}
