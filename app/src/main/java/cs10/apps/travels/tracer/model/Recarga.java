package cs10.apps.travels.tracer.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Recarga {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private int year, month, day;
    private double mount;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public double getMount() {
        return mount;
    }

    public void setMount(double mount) {
        this.mount = mount;
    }
}
