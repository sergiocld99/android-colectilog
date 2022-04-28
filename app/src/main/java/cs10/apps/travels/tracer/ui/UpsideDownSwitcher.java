package cs10.apps.travels.tracer.ui;

import android.content.Context;
import android.os.Handler;
import android.widget.TextSwitcher;


import cs10.apps.travels.tracer.R;
import cs10.apps.travels.tracer.model.ScheduledParada;

public class UpsideDownSwitcher {
    private TextSwitcher tvSwitcher;
    private ScheduledParada item;
    private Context context;
    private Runnable r;
    private Handler h;

    public void setContext(Context context) {
        this.context = context;
    }

    public void setTvSwitcher(TextSwitcher tvSwitcher) {
        this.tvSwitcher = tvSwitcher;
    }

    public void setItem(ScheduledParada item) {
        this.item = item;
    }

    public void startAnimation(){
        h = new Handler();

        r = () -> {
            try {
                if (item.switched) {
                    // Slide down
                    tvSwitcher.setInAnimation(context, R.anim.slide_down_in);
                    tvSwitcher.setOutAnimation(context, R.anim.slide_down_out);
                    tvSwitcher.setText("Arrives at " + item.getNextArrival());
                } else {
                    // Slide up
                    tvSwitcher.setInAnimation(context, R.anim.slide_up_in);
                    tvSwitcher.setOutAnimation(context, R.anim.slide_up_out);
                    tvSwitcher.setText("Next is " + item.getTransportInfo());
                }
            } catch (Exception e){
                stop();
            } finally {
                item.switched = !item.switched;
                h.postDelayed(r, 3000);
            }
        };

        r.run();
    }

    public void stop(){
        h.removeCallbacks(r);
    }
}
