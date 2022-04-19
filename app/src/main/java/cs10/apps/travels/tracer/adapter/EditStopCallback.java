package cs10.apps.travels.tracer.adapter;

import android.content.Context;

public interface EditStopCallback {

    Context getContext();
    void onEdit(String stopName);
}
