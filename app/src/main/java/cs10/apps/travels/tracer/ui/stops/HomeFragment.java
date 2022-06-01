package cs10.apps.travels.tracer.ui.stops;

import android.location.Location;
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
import cs10.apps.travels.tracer.DrawerActivity;
import cs10.apps.travels.tracer.Utils;
import cs10.apps.travels.tracer.adapter.LocatedArrivalsAdapter;
import cs10.apps.travels.tracer.databinding.FragmentHomeBinding;
import cs10.apps.travels.tracer.db.MiDB;
import cs10.apps.travels.tracer.model.Parada;
import cs10.apps.travels.tracer.model.Viaje;
import cs10.apps.travels.tracer.model.roca.ArriboTren;
import cs10.apps.travels.tracer.model.roca.HorarioTren;
import cs10.apps.travels.tracer.model.roca.RamalSchedule;

public class HomeFragment extends CS_Fragment {
    private FragmentHomeBinding binding;
    private LocatedArrivalsAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new LocatedArrivalsAdapter();
        adapter.setContext(getContext());

        binding.recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recycler.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.recycler.setVisibility(View.GONE);
        binding.pbar.setVisibility(View.VISIBLE);

        if (getActivity() instanceof DrawerActivity){
            DrawerActivity activity = (DrawerActivity) getActivity();
            activity.requestCurrentLocation().addOnSuccessListener(location -> {
                if (location != null) doInBackground(() -> onBuildHome(location));
            });
        }
    }

    public void onBuildHome(Location location){
        if (getActivity() instanceof DatabaseCallback){
            DatabaseCallback callback = (DatabaseCallback) getActivity();
            MiDB miDB = callback.getInstanceWhenFinished();

            List<Parada> favourites = miDB.paradasDao().getFavouriteStops();
            Utils.orderByProximity(favourites, location.getLatitude(), location.getLongitude());
            if (favourites.isEmpty()) return;

            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int m = calendar.get(Calendar.MINUTE);
            int now = hour * 60 + m;

            String stopName = favourites.get(0).getNombre();
            List<Viaje> arrivals = miDB.viajesDao().getNextArrivals(stopName, hour, m);
            List<RamalSchedule> trenes = miDB.servicioDao().getNextArrivals(stopName, hour, m);

            for (RamalSchedule tren : trenes){
                ArriboTren v = new ArriboTren();
                int target = tren.getHour() * 60 + tren.getMinute();
                HorarioTren end = miDB.servicioDao().getFinalStation(tren.getService());

                v.setTipo(1);
                v.setRamal(tren.getRamal());
                v.setStartHour(tren.getHour());
                v.setStartMinute(tren.getMinute());
                v.setNombrePdaFin(end.getStation());
                v.setRecorrido(miDB.servicioDao().getRecorridoUntil(tren.getService(), now, target));
                v.getRecorrido().add(end);
                v.restartAux();
                arrivals.add(v);
            }

            Collections.sort(arrivals);

            doInForeground(() -> {
                binding.tvTitle.setText("Próximos en " + stopName);
                binding.pbar.setVisibility(View.GONE);
                binding.recycler.setVisibility(View.VISIBLE);
                adapter.setViajes(arrivals);
                adapter.notifyDataSetChanged();
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}