package cs10.apps.travels.tracer.db;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Calendar;
import java.util.List;

import cs10.apps.travels.tracer.model.Viaje;
import cs10.apps.travels.tracer.model.roca.HorarioTren;
import cs10.apps.travels.tracer.model.roca.RamalSchedule;

public class DynamicQuery {
    private static MiDB db;

    private static MiDB getDb(Context context){
        if (db == null) db = MiDB.getInstance(context);
        return db;
    }

    private static SharedPreferences getTrainsPrefs(Context context){
        return context.getSharedPreferences("trains", Context.MODE_PRIVATE);
    }

    private static SharedPreferences getBusPrefs(Context context){
        return context.getSharedPreferences("buses", Context.MODE_PRIVATE);
    }

    public static HorarioTren findCombination(Context context, String targetRamal, HorarioTren current){
        int target = current.getHour() * 60 + current.getMinute();
        int maxWait = getTrainsPrefs(context).getInt("maxWait", 10);
        return getDb(context).servicioDao().getArrival(targetRamal, current.getStation(), target, maxWait);
    }

    public static List<RamalSchedule> getNextTrainArrivals(Context context, String stopName){
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int target = hour * 60 + minute;
        int cant = getTrainsPrefs(context).getInt("maxArrivals", 10);
        int timelapse = getTrainsPrefs(context).getInt("timelapse", 60);

        return getDb(context).servicioDao().getNextArrivals(stopName, target, cant, timelapse);
    }

    public static List<Viaje> getNextBusArrivals(Context context, String stopName){
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int target = hour * 60 + minute;
        int cant = getBusPrefs(context).getInt("maxArrivals", 6);
        int timelapse = getBusPrefs(context).getInt("timelapse", 600);

        return getDb(context).viajesDao().getNextArrivals(stopName, target, cant, timelapse);
    }
}
