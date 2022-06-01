package cs10.apps.travels.tracer.model.roca;

import java.util.List;

import cs10.apps.travels.tracer.model.Viaje;

public class ArriboTren extends Viaje {

    private List<HorarioTren> recorrido;
    private int aux = 0;

    public void restartAux(){
        aux = recorrido.size();
    }

    public void incrementAux(){
        if (aux < recorrido.size()) aux++;
        else aux = 0;
    }

    @Override
    public String getLineInformation() {
        return getRamal();
    }

    public List<HorarioTren> getRecorrido() {
        return recorrido;
    }

    public void setRecorrido(List<HorarioTren> recorrido) {
        this.recorrido = recorrido;
    }

    public int getAux() {
        return aux;
    }

    public void setAux(int aux) {
        this.aux = aux;
    }
}
