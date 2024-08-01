package cs10.apps.travels.tracer;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.Comparator;
import java.util.List;

import cs10.apps.common.android.Localizable;
import cs10.apps.travels.tracer.enums.TransportType;
import cs10.apps.travels.tracer.model.Parada;
import cs10.apps.travels.tracer.model.Viaje;
import cs10.apps.travels.tracer.model.joins.TravelStats;
import cs10.apps.travels.tracer.utils.LocaleDecimalFormat;


public class Utils {
    private static final LocaleDecimalFormat df = new LocaleDecimalFormat(2);

    private static final LocaleDecimalFormat rf = new LocaleDecimalFormat(1);

    @NonNull
    public static String twoDecimals(int value) {
        return value < 10 ? "0" + value : String.valueOf(value);
    }

    public static String rateFormat(double rate){
        return rf.format(rate);
    }

    public static String priceFormat(double value) {
        return "$" + df.format(Math.round(value * 100) / 100d);
    }

    public static boolean checkPermissions(Activity activity) {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                activity.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }

            return false;
        }

        return true;
    }

    public static String simplify(String stationName){
        return stationName.replace("Estación","").trim();
    }

    /**
     * Updates the distance of each stop and sorts the list from closest to farthest
     */
    public static void orderByProximity(@NonNull List<Parada> list, Double latitude, Double longitude){
        for (Parada p : list) p.updateDistance(latitude, longitude);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            list.sort(Comparator.comparingDouble(Parada::getDistance));
        }
    }

    public static long bestRound(Double value){
        if (Math.abs(value) > 0.01) return Math.round(value * 100) * 10;
        else return Math.round(value * 1000);
    }

    public static int colorForType(double type){
        return colorForType((int) Math.round(type));
    }

    public static int colorForType(int type){
        switch (type){
            case 0: return R.color.bus;
            case 1: return R.color.train;
            case 2: return R.color.bus_159;
            default: return R.color.black;
        }
    }

    public static void paintBusColor(int color, CardView view){
        if (color == 0)
            view.setCardBackgroundColor(ContextCompat.getColor(view.getContext(), R.color.bus));
        else
            view.setCardBackgroundColor(color);
    }

    public static void paintBusColor(int color, View view){
        if (color == 0)
            view.setBackgroundColor(ContextCompat.getColor(view.getContext(), R.color.bus));
        else
            view.setBackgroundColor(color);
    }

    @Deprecated
    public static int colorFor(Integer bus){
        if (bus == null) return R.color.black;

        switch (bus){
            case 202:
                return R.color.bus_202;
            case 324:
                return R.color.bus_324;
            case 160:
            case 178:
            case 414:
                return R.color.bus_414;
            case 159:
            case 603:
                return R.color.bus_159;
            case 383:
            case 500:
            case 508:
                return R.color.bus_500;
            case 98:
            case 148:
                return R.color.bus_98;
            default:
                return R.color.bus;
        }
    }

    public static String dateFormat(int day, int month, int year) {
        return day + "/" + month + "/" + year;
    }

    public static CharSequence hourFormat(Calendar calendar){
        return hourFormat(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
    }

    public static CharSequence hourFormat(int hour, int minute) {
        return hour + ":" + twoDecimals(minute);
    }

    public static void setWeekDay(Viaje v){
        Calendar calendar = Calendar.getInstance();
        calendar.set(v.getYear(), v.getMonth()-1, v.getDay());    // 0 es Enero en Calendar
        v.setWeekDay(calendar.get(Calendar.DAY_OF_WEEK));
    }

    public static String getWdCompleteString(int weekDay){
        switch (weekDay) {
            case Calendar.SUNDAY: return "Domingo";
            case Calendar.MONDAY: return "Lunes";
            case Calendar.TUESDAY: return "Martes";
            case Calendar.WEDNESDAY: return "Miércoles";
            case Calendar.THURSDAY: return "Jueves";
            case Calendar.FRIDAY: return "Viernes";
            case Calendar.SATURDAY: return "Sábado";
            default: return "";
        }
    }

    public static String getWeekDayString(int weekDay) {
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

    public static Drawable getTypeDrawable(int type, Context context){
        if (type < 0 || type > 3) return null;
        TransportType tt = TransportType.values()[type];
        int drawableId;

        switch (tt){
            case BUS:
                drawableId = R.drawable.ic_bus;
                break;
            case TRAIN:
                drawableId = R.drawable.ic_train;
                break;
            case CAR:
                drawableId = R.drawable.ic_car;
                break;
            case METRO:
                drawableId = R.drawable.ic_railway;
                break;
            default:
                return null;
        }

        return ContextCompat.getDrawable(context, drawableId);
    }

    public static Drawable getTypeDrawable(TransportType type, Context context){
        return getTypeDrawable(type.ordinal(), context);
    }

    public static void loadTrainBanner(ImageView iv){
        Picasso.get().load("https://www.el1digital.com.ar/wp-content/uploads/2021/12/b1-51.jpg")
                .memoryPolicy(MemoryPolicy.NO_CACHE).into(iv);
    }

    public static void loadMetroBanner(ImageView iv){
        Picasso.get().load("https://buenosaires.gob.ar/sites/default/files/styles/card_img_top/public/2023-06/L%C3%ADnea%20A_0.jpg?itok=Ij83SMOT"
        ).memoryPolicy(MemoryPolicy.NO_CACHE).into(iv);
    }

    public static void loadBusBanner(ImageView iv){
        Picasso.get().load("https://mir-s3-cdn-cf.behance.net/project_modules/max_1200/d037df20161495.562e68da93c72.jpg")
                .memoryPolicy(MemoryPolicy.NO_CACHE).into(iv);
    }

    public static void loadSubeBanner(@NotNull ImageView appbarImage) {
        Picasso.get().load("https://media.ambito.com/p/5fa4f0e7d7842a07e938f45577916a2d/adjuntos/239/imagenes/039/693/0039693734/subejpg.jpg")
                .memoryPolicy(MemoryPolicy.NO_CACHE).into(appbarImage);
    }

    public static void loadCoffeeBanner(@NonNull ImageView view) {
        Picasso.get().load("https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fcdn.computerhoy.com%2Fsites%2Fnavi.axelspringer.es%2Fpublic%2Fstyles%2F480%2Fpublic%2Fmedia%2Fimage%2F2018%2F03%2F294127-tazas-cafe-recomendables-dia.jpg%3Fitok%3DdbraWqYX&f=1&nofb=1")
                .memoryPolicy(MemoryPolicy.NO_CACHE).into(view);
    }

    public static int getCurrentTs(){
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);
    }

    public static double getRealCurrentTs(){
        Calendar calendar = Calendar.getInstance();
        int currentSeconds = calendar.get(Calendar.HOUR_OF_DAY) * 3600 +
                calendar.get(Calendar.MINUTE) * 60 + calendar.get(Calendar.SECOND);

        return currentSeconds / 60d;
    }

    // v2.21
    public static Double calculateAverageSpeed(List<TravelStats> stats){
        if (stats.isEmpty()) return null;

        double sum = 0.0;
        for (TravelStats s : stats) sum += s.calculateSpeedInKmH();
        return sum / stats.size();
    }

    public static Direction getDirection(double x0, double y0, double x1, double y1){
        double diff_x = x1 - x0;
        double diff_y = y1 - y0;

        // va hacia el norte
        if (diff_x > 0){
            if (diff_y > 0) return Direction.NORTH_EAST;
            else return Direction.NORTH_WEST;
        } else {
            if (diff_y > 0) return Direction.SOUTH_EAST;
            else return Direction.SOUTH_WEST;
        }
    }

    public static Direction getDirection(@NonNull Localizable start, @NonNull Localizable end){
        return getDirection(start.getX(), start.getY(), end.getX(), end.getY());
    }

    public enum Direction {
        NORTH_EAST, NORTH_WEST, SOUTH_EAST, SOUTH_WEST
    }
}
