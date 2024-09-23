package cs10.apps.travels.tracer.legacy.next.components;

import android.os.Handler;
import android.widget.TextSwitcher;

import cs10.apps.travels.tracer.R;
import cs10.apps.travels.tracer.model.joins.ScheduledParada;

public class UpsideDownSwitcher {
    private TextSwitcher tvSwitcher;
    private ScheduledParada item;
    private Runnable r;
    private Handler h;

    public void setTvSwitcher(TextSwitcher tvSwitcher) {
        this.tvSwitcher = tvSwitcher;
    }

    public void setItem(ScheduledParada item) {
        this.item = item;
    }

    public void startAnimation(){
        if (h != null) return;
        h = new Handler();

        r = () -> {
            try {
                if (item.switched) {
                    // Slide down
                    tvSwitcher.setInAnimation(tvSwitcher.getContext(), R.anim.slide_down_in);
                    tvSwitcher.setOutAnimation(tvSwitcher.getContext(), R.anim.slide_down_out);
                    tvSwitcher.setText(tvSwitcher.getContext().getString(R.string.arrives_at, item.getNextArrival()));
                } else {
                    // Slide up
                    tvSwitcher.setInAnimation(tvSwitcher.getContext(), R.anim.slide_up_in);
                    tvSwitcher.setOutAnimation(tvSwitcher.getContext(), R.anim.slide_up_out);
                    tvSwitcher.setText(tvSwitcher.getContext().getString(R.string.next_is, item.getRamal()));
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
        if (h != null){
            h.removeCallbacks(r);
            h = null;
        }
    }
}
