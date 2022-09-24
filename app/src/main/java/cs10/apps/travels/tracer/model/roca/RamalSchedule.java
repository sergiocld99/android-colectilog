package cs10.apps.travels.tracer.model.roca;

public class RamalSchedule extends HorarioTren implements Comparable<RamalSchedule> {

    private String cabecera;
    private String ramal;

    public String getCabecera() {
        return cabecera;
    }

    public void setCabecera(String cabecera) {
        this.cabecera = cabecera;
    }

    public String getRamal() {
        return ramal;
    }

    public void setRamal(String ramal) {
        this.ramal = ramal;
    }

    @Override
    public int compareTo(RamalSchedule ramalSchedule) {
        int val0 = Integer.compare(this.getHour(), ramalSchedule.getHour());
        return val0 == 0 ? Integer.compare(this.getMinute(), ramalSchedule.getMinute()) : val0;
    }
}
