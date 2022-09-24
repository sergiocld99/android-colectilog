package cs10.apps.travels.tracer.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import java.util.Calendar;

import cs10.apps.travels.tracer.Utils;

@Entity(foreignKeys = {
        @ForeignKey(entity = Parada.class, parentColumns = "nombre", childColumns = "nombrePdaInicio"),
        @ForeignKey(entity = Parada.class, parentColumns = "nombre", childColumns = "nombrePdaFin")
})
public class Viaje implements Comparable<Viaje> {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private int day, month, year;
    private int startHour, startMinute;

    @Nullable
    private Integer endHour, endMinute;

    private int tipo;

    @Nullable
    private Integer linea;

    @Nullable
    private String ramal;

    @NonNull
    private String nombrePdaInicio = "Inicio", nombrePdaFin = "Fin";

    private double costo;

    @ColumnInfo(name = "wd")
    private int weekDay;

    public double getCosto() {
        return costo;
    }

    public void setCosto(double costo) {
        this.costo = costo;
    }

    public int getWeekDay() {
        return weekDay;
    }

    public String getWeekDayString() {
        switch (weekDay) {
            case Calendar.SUNDAY: return "Dom ";
            case Calendar.MONDAY: return "Lun ";
            case Calendar.TUESDAY: return "Mar ";
            case Calendar.WEDNESDAY: return "Mie ";
            case Calendar.THURSDAY: return "Jue ";
            case Calendar.FRIDAY: return "Vie ";
            case Calendar.SATURDAY: return "Sab ";
            default: return "";
        }
    }

    public void setWeekDay(int weekDay) {
        this.weekDay = weekDay;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getStartHour() {
        return startHour;
    }

    public void setStartHour(int startHour) {
        this.startHour = startHour;
    }

    public int getStartMinute() {
        return startMinute;
    }

    public void setStartMinute(int startMinute) {
        this.startMinute = startMinute;
    }

    @Nullable
    public Integer getEndHour() {
        return endHour;
    }

    public void setEndHour(@Nullable Integer endHour) {
        this.endHour = endHour;
    }

    @Nullable
    public Integer getEndMinute() {
        return endMinute;
    }

    public void setEndMinute(@Nullable Integer endMinute) {
        this.endMinute = endMinute;
    }

    public int getTipo() {
        return tipo;
    }

    public void setTipo(int tipo) {
        this.tipo = tipo;
    }

    @Nullable
    public Integer getLinea() {
        return linea;
    }

    public void setLinea(@Nullable Integer linea) {
        this.linea = linea;
    }

    @Nullable
    public String getRamal() {
        return ramal;
    }

    public void setRamal(@Nullable String ramal) {
        this.ramal = ramal;
    }

    @NonNull
    public String getNombrePdaInicio() {
        return nombrePdaInicio;
    }

    public void setNombrePdaInicio(@NonNull String nombrePdaInicio) {
        this.nombrePdaInicio = nombrePdaInicio;
    }

    @NonNull
    public String getNombrePdaFin() {
        return nombrePdaFin;
    }

    public void setNombrePdaFin(@NonNull String nombrePdaFin) {
        this.nombrePdaFin = nombrePdaFin;
    }

    public String getStartTimeString() {
        String day = getWeekDayString() + getDay() + "/" + getMonth();
        CharSequence start = Utils.hourFormat(getStartHour(), getStartMinute());
        CharSequence end = getEndHour() != null ? Utils.hourFormat(getEndHour(), getEndMinute()) : null;

        return day + " - " + start + (end != null ? " / " + end : "");
    }

    public String getStartAndEnd() {
        return getNombrePdaInicio() + " - " + getNombrePdaFin();
    }

    public String getLineInformation() {
        if (getLinea() == null) return null;
        return "Línea " + getLinea() + (getRamal() != null ? " - " + getRamal() : "");
    }

    public String getLineSimplified(){
        if (getLinea() == null) return "Roca";
        else return String.valueOf(getLinea());
    }

    public String getLineInfoAndPrice() {
        String li = getLineInformation();
        if (getCosto() == 0) return li;
        if (li == null) return Utils.priceFormat(getCosto());
        return li + " (" + Utils.priceFormat(getCosto()) + ")";
    }

    public String getRamalAndPrice(){
        if (getRamal() == null) return Utils.priceFormat(getCosto());
        else return getRamal() + " (" + Utils.priceFormat(getCosto()) + ")";
    }

    @Override
    public int compareTo(Viaje viaje) {
        int r = Integer.compare(this.startHour, viaje.startHour);
        return r == 0 ? Integer.compare(this.startMinute, viaje.startMinute) : r;
    }

    // ---------------------------- LIVE ----------------------

    public int getDuration(){
        if (endHour != null && endMinute != null) {
            int end = endHour * 60 + endMinute;
            int start = startHour * 60 + startMinute;
            return end - start;
        }

        return 0;
    }


}
