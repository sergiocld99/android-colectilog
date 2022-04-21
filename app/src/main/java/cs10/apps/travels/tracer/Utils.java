package cs10.apps.travels.tracer;

import androidx.annotation.NonNull;

public class Utils {

    @NonNull
    public static String twoDecimals(int value){
        return value < 10 ? "0" + value : String.valueOf(value);
    }

    @NonNull
    public static String removeParentheses(@NonNull String str) {
        String updated = str.replaceAll("\\([^()]*\\)", "");
        if (updated.contains("(")) updated = removeParentheses(updated);
        return updated.trim();
    }

    public static int colorFor(Integer bus){
        if (bus == null) return R.color.train;

        switch (bus){
            case 324:
                return R.color.bus_324;
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
}
