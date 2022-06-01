package cs10.apps.travels.tracer.model.roca;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/*
    Para la primera versión, solo modelamos el Vía Circuito (Plaza - Temperley - Quilmes - Plaza)
    Además solo consideramos los horarios de días habiles.

    Podríamos tener únicamente cada horario para cada estación (arribo), pero esto no nos alcanza
    para conocer en qué SENTIDO está viajando el tren, incluso agregando un "ordinal".

    Es necesario saber a qué "servicio" pertenece cada arribo. El servicio se identifica por la
    parada y hora donde arrancó. No usamos número de tren porque es relativo al día.

    El servicio debe usarse para cada arribo almacenado. Si usamos una clave primaria compuesta
    estaremos gastando mucho espacio (3 columnas), entonces mejor usamos un ID.

    Ahora bien, el ramal con sus paradas se conoce perfectamente mediante consultas de horario
    e ID de servicio, pero no se tiene un nombre formal para mostrar al usuario. Se necesita
    crear otra tabla que contenga los nombres, o bien, agregar la columna para servicio.
 */

@Entity
public class ServicioTren {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String cabecera;
    private int hora, minuto;

    // agregado en migración 17
    private String ramal;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCabecera() {
        return cabecera;
    }

    public void setCabecera(String cabecera) {
        this.cabecera = cabecera;
    }

    public int getHora() {
        return hora;
    }

    public void setHora(int hora) {
        this.hora = hora;
    }

    public int getMinuto() {
        return minuto;
    }

    public void setMinuto(int minuto) {
        this.minuto = minuto;
    }

    public void setTime(int hora, int minuto) {
        setHora(hora);
        setMinuto(minuto);
    }

    public String getRamal() {
        return ramal;
    }

    public void setRamal(String ramal) {
        this.ramal = ramal;
    }
}
