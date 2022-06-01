package cs10.apps.travels.tracer.ui.stops;

import android.os.Handler;
import android.widget.TextSwitcher;

import java.util.Calendar;

import cs10.apps.travels.tracer.R;
import cs10.apps.travels.tracer.Utils;
import cs10.apps.travels.tracer.model.roca.ArriboTren;
import cs10.apps.travels.tracer.model.roca.HorarioTren;

public class ETA_Switcher {
    private TextSwitcher tvSwitcher;
    private ArriboTren item;
    private DepartCallback callback;
    private Runnable r;
    private Handler h;

    public void setCallback(DepartCallback callback) {
        this.callback = callback;
    }

    public void setTvSwitcher(TextSwitcher tvSwitcher) {
        this.tvSwitcher = tvSwitcher;
    }

    public void setItem(ArriboTren item) {
        this.item = item;
    }

    public void startAnimation(){
        if (h != null) return;
        h = new Handler();

        r = () -> {
            try {
                if (item.getAux() >= item.getRecorrido().size()) {
                    Calendar calendar = Calendar.getInstance();
                    int currentM = calendar.get(Calendar.MINUTE);
                    int currentH = calendar.get(Calendar.HOUR_OF_DAY);
                    int diffM = 60 * (item.getStartHour() - currentH) + item.getStartMinute() - currentM;

                    // remove old data
                    while (!item.getRecorrido().isEmpty() && isOld(item.getRecorrido().get(0), currentH, currentM)){
                        item.getRecorrido().remove(0);
                    }

                    // Slide down
                    tvSwitcher.setInAnimation(callback.getContext(), R.anim.slide_down_in);
                    tvSwitcher.setOutAnimation(callback.getContext(), R.anim.slide_down_out);
                    if (diffM > 0) tvSwitcher.setText("Llega al andén en " + diffM + " minutos");
                    else if (diffM == 0) tvSwitcher.setText("¡Ahora mismo en andén!");
                    else {
                        stop();
                        callback.onDepart();
                        return;
                    }
                } else {
                    HorarioTren horarioC = item.getRecorrido().get(item.getAux());

                    // Slide up
                    tvSwitcher.setInAnimation(callback.getContext(), R.anim.slide_up_in);
                    tvSwitcher.setOutAnimation(callback.getContext(), R.anim.slide_up_out);
                    tvSwitcher.setText(Utils.hourFormat(horarioC.getHour(), horarioC.getMinute()) + " - " + horarioC.getStation());
                }

                item.incrementAux();
                h.postDelayed(r, item.getAux() == 0 ? 8000 : 4000);

            } catch (Exception e){
                stop();
            }
        };

        r.run();
    }

    private boolean isOld(HorarioTren horarioTren, int currentH, int currentM) {
        return 60 * (horarioTren.getHour() - currentH) + horarioTren.getMinute() - currentM < 0;
    }

    public void stop(){
        if (h != null){
            h.removeCallbacks(r);
            h = null;
        }
    }
}
