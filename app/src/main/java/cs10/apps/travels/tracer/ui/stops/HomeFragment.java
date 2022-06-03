package cs10.apps.travels.tracer.ui.stops;

import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

import cs10.apps.common.android.CS_Fragment;
import cs10.apps.travels.tracer.DrawerActivity;
import cs10.apps.travels.tracer.Utils;
import cs10.apps.travels.tracer.adapter.LocatedArrivalsAdapter;
import cs10.apps.travels.tracer.databinding.FragmentHomeBinding;
import cs10.apps.travels.tracer.db.MiDB;
import cs10.apps.travels.tracer.model.Parada;

public class HomeFragment extends CS_Fragment {
    private FragmentHomeBinding binding;
    private LocatedArrivalsAdapter adapter;
    private HomeSliderAdapter sliderAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sliderAdapter = new HomeSliderAdapter(this);
        binding.viewPager.setAdapter(sliderAdapter);

        /*
        adapter = new LocatedArrivalsAdapter();
        adapter.setContext(getContext());

        binding.recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recycler.setAdapter(adapter);
         */
    }

    @Override
    public void onResume() {
        super.onResume();
        // binding.recycler.setVisibility(View.GONE);
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

            doInForeground(() -> {
                binding.pbar.setVisibility(View.GONE);
                sliderAdapter.setFavourites(favourites);
                sliderAdapter.notifyDataSetChanged();
            });

            /*
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
                v.setRecorridoDestino(miDB.servicioDao().getRecorridoFrom(tren.getService(), target));
                v.setEndHour(end.getHour());
                v.setEndMinute(end.getMinute());
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
             */
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private class HomeSliderAdapter extends FragmentStateAdapter {
        private List<Parada> favourites;

        public HomeSliderAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        public void setFavourites(List<Parada> favourites) {
            this.favourites = favourites;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            StopArrivalsFragment fragment = new StopArrivalsFragment();
            fragment.setStopName(favourites.get(position).getNombre());
            return fragment;
        }

        @Override
        public int getItemCount() {
            return favourites == null ? 0 : favourites.size();
        }
    }
}