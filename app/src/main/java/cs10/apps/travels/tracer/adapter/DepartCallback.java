package cs10.apps.travels.tracer.adapter;

import cs10.apps.travels.tracer.model.roca.ArriboTren;

public interface DepartCallback extends AdapterCallback {

    void onDepart(ArriboTren item);
}
