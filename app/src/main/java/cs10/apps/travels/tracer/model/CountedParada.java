package cs10.apps.travels.tracer.model;

import androidx.room.ColumnInfo;

public class CountedParada extends Parada {

    @ColumnInfo(name = "veces")
    private int veces;

    public int getVeces() {
        return veces;
    }

    public void setVeces(int veces) {
        this.veces = veces;
    }
}
