package cs10.apps.common.android;

public class CS_Time {
    private int hour;
    private int minute;

    public void setFromString(String strInHourFormat){
        String[] params = strInHourFormat.split(":");
        setHour(Integer.parseInt(params[0]));
        setMinute(Integer.parseInt(params[1]));
    }

    public void add(int minutes){
        this.minute += minutes;

        if (this.minute >= 60){
            this.hour++;
            this.minute -= 60;
        }
    }

    public void addPositive(int minutes){
        while (minutes < 0) minutes += 60;
        this.add(minutes);
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }
}
