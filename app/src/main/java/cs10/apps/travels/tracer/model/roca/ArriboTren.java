package cs10.apps.travels.tracer.model.roca;

import java.util.List;

import cs10.apps.travels.tracer.data.generator.Station;
import cs10.apps.travels.tracer.model.Viaje;

public class ArriboTren extends Viaje {

    private List<HorarioTren> recorrido;
    private List<HorarioTren> recorridoDestino;
    private long serviceId;
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

    public long getServiceId() {
        return serviceId;
    }

    public void setServiceId(long serviceId) {
        this.serviceId = serviceId;
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

    public boolean estaEnCabecera(){
        if (recorrido.isEmpty()) return false;
        return getNombrePdaInicio().equals(getTarget().getStation());
    }

    public HorarioTren getTarget(){
        return recorrido.get(recorrido.size()-1);
    }
}
