package cs10.apps.travels.tracer.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "trenes")
public class Tren {

    @PrimaryKey
    public int coche;

    @ColumnInfo(name = "realiza")
    public int idCircuitoRealizado;

    // public boolean sentidoCircuito;

    public Tren(){}

    @Ignore
    public Tren(int coche, @NonNull Circuito circuito){
        this.coche = coche;
        this.idCircuitoRealizado = circuito.id;
        // this.setSentido(sentido);
    }

    public Sentido getSentido(){
        if (coche % 2 == 0) return Sentido.HACIA_FIN;
        return Sentido.HACIA_INICIO;
    }
}
