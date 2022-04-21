package cs10.apps.travels.tracer.model;

import androidx.room.ColumnInfo;

public class DetailedParada extends Parada {

    @ColumnInfo(name = "veces")
    private int veces;

    public int getVeces() {
        return veces;
    }

    public void setVeces(int veces) {
        this.veces = veces;
    }
}
