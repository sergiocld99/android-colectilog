package cs10.apps.travels.tracer;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;

import cs10.apps.travels.tracer.model.Parada;
import cs10.apps.travels.tracer.model.Viaje;

public class Utils {
    private static final DecimalFormat df = new DecimalFormat("0.00");

    @NonNull
    public static String twoDecimals(int value) {
        return value < 10 ? "0" + value : String.valueOf(value);
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

    public static int colorFor(Integer bus){
        if (bus == null) return R.color.train;

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

    public static CharSequence hourFormat(int hour, int minute) {
        return hour + ":" + twoDecimals(minute);
    }

    public static void setWeekDay(Viaje v){
        Calendar calendar = Calendar.getInstance();
        calendar.set(v.getYear(), v.getMonth()-1, v.getDay());    // 0 es Enero en Calendar
        v.setWeekDay(calendar.get(Calendar.DAY_OF_WEEK));
    }

    public static Drawable getTypeDrawable(int type, Context context){
        return ContextCompat.getDrawable(context, type == 0 ? R.drawable.ic_bus : R.drawable.ic_train);
    }

    public static void loadTrainBanner(ImageView iv){
        Picasso.get().load("https://www.el1digital.com.ar/wp-content/uploads/2021/12/b1-51.jpg")
                .memoryPolicy(MemoryPolicy.NO_CACHE).into(iv);
    }

    public static void loadBusBanner(ImageView iv){
        Picasso.get().load("https://www.infoblancosobrenegro.com/uploads/noticias/5/2022/07/20220708100904_talp.jpg")
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
}
