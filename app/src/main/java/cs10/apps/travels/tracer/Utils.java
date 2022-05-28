package cs10.apps.travels.tracer;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.List;

import cs10.apps.travels.tracer.model.Parada;

public class Utils {
    private static final DecimalFormat df = new DecimalFormat("#.00");

    @NonNull
    public static String twoDecimals(int value){
        return value < 10 ? "0" + value : String.valueOf(value);
    }

    public static String priceFormat(double value){
        return "$" + df.format(Math.round(value * 100) / 100d);
    }

    @NonNull
    public static String removeParentheses(@NonNull String str) {
        String updated = str.replaceAll("\\([^()]*\\)", "");
        if (updated.contains("(")) updated = removeParentheses(updated);
        return updated.trim();
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

    public static void orderByProximity(@NonNull List<Parada> list, Double latitude, Double longitude){
        for (Parada p : list){
            p.setDeltaX(p.getLatitud() - latitude);
            p.setDeltaY(p.getLongitud() - longitude);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            list.sort(Comparator.comparingDouble(Parada::getDistance));
        }
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
}
