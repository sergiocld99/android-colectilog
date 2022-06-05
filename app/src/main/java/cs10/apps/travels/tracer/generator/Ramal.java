package cs10.apps.travels.tracer.generator;

public enum Ramal {
    BOSQUES_T_QUILMES("Bosques T > Quilmes"),
    BOSQUES_Q_TEMPERLEY("Bosques Q > Temperley");

    private final String nombre;

    Ramal(String nombre){
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }
}
