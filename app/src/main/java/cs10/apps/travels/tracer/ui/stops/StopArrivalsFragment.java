package cs10.apps.travels.tracer.ui.stops;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import cs10.apps.common.android.CS_Fragment;
import cs10.apps.travels.tracer.R;
import cs10.apps.travels.tracer.Utils;
import cs10.apps.travels.tracer.adapter.LocatedArrivalsAdapter;
import cs10.apps.travels.tracer.adapter.ServiceCallback;
import cs10.apps.travels.tracer.databinding.FragmentArrivalsBinding;
import cs10.apps.travels.tracer.db.DynamicQuery;
import cs10.apps.travels.tracer.db.MiDB;
import cs10.apps.travels.tracer.model.Viaje;
import cs10.apps.travels.tracer.model.roca.ArriboTren;
import cs10.apps.travels.tracer.model.roca.HorarioTren;
import cs10.apps.travels.tracer.model.roca.RamalSchedule;
import cs10.apps.travels.tracer.ui.service.ServiceDetail;

public class StopArrivalsFragment extends CS_Fragment implements ServiceCallback {
    private FragmentArrivalsBinding binding;
    private LocatedArrivalsAdapter adapter;
    private String stopName;
    private double proximity;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentArrivalsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new LocatedArrivalsAdapter();
        adapter.setCallback(this);

        binding.recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recycler.setAdapter(adapter);
    }

    public void setStopName(String stopName) {
        this.stopName = stopName;
    }

    @Override
    public void onResume() {
        super.onResume();

        doInBackground(() -> {
            MiDB miDB = MiDB.getInstance(getContext());
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int m = calendar.get(Calendar.MINUTE);
            int now = hour * 60 + m;

            List<Viaje> arrivals = DynamicQuery.getNextBusArrivals(getContext(), stopName);
            List<RamalSchedule> trenes = DynamicQuery.getNextTrainArrivals(getContext(), stopName);

            for (RamalSchedule tren : trenes){
                ArriboTren v = new ArriboTren();
                int target = tren.getHour() * 60 + tren.getMinute();
                HorarioTren end = miDB.servicioDao().getFinalStation(tren.getService());

                v.setTipo(1);
                v.setRamal(tren.getRamal());
                v.setStartHour(tren.getHour());
                v.setStartMinute(tren.getMinute());
                v.setServiceId(tren.getService());
                v.setNombrePdaFin(Utils.simplify(end.getStation()));
                v.setNombrePdaInicio(tren.getCabecera());
                v.setRecorrido(miDB.servicioDao().getRecorridoUntil(tren.getService(), now, target));
                v.setRecorridoDestino(miDB.servicioDao().getRecorridoFrom(tren.getService(), target));
                v.setEndHour(end.getHour());
                v.setEndMinute(end.getMinute());
                v.restartAux();
                arrivals.add(v);
            }

            Collections.sort(arrivals);

            doInForeground(() -> {
                binding.tvTitle.setText(getString(R.string.next_ones_in, stopName));
                binding.tvSubtitle.setText(Math.round(proximity*100) + "% de cercanía");
                adapter.setViajes(arrivals);
                adapter.notifyDataSetChanged();
            });
        });
    }

    @Override
    public void onServiceSelected(long id, String ramal) {
        Intent intent = new Intent(getActivity(), ServiceDetail.class);
        intent.putExtra("station", stopName);
        intent.putExtra("ramal", ramal);
        intent.putExtra("id", id);
        startActivity(intent);
    }

    public void setProximity(double v) {
        this.proximity = v;
    }
}
