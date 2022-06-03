package cs10.apps.travels.tracer.model.roca;

import java.util.List;

import cs10.apps.travels.tracer.db.filler.Station;
import cs10.apps.travels.tracer.model.Viaje;

public class ArriboTren extends Viaje {

    private List<HorarioTren> recorrido;
    private List<HorarioTren> recorridoDestino;
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

    public List<HorarioTren> getRecorridoDestino() {
        return recorridoDestino;
    }

    public void setRecorridoDestino(List<HorarioTren> recorridoDestino) {
        this.recorridoDestino = recorridoDestino;
    }

    public int getAux() {
        return aux;
    }

    public void setAux(int aux) {
        this.aux = aux;
    }

    public boolean isFutureStation(Station station) {
        for (HorarioTren h : recorridoDestino) {
            if (h.getStation().equals(station.getNombre())) return true;
        }

        return false;
    }
}
