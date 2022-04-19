package cs10.apps.travels.tracer.model;

public enum Sentido {
    HACIA_INICIO(true), HACIA_FIN(false);

    private final boolean bool;

    Sentido(boolean bool){
        this.bool = bool;
    }

    public boolean bool(){
        return bool;
    }
}
