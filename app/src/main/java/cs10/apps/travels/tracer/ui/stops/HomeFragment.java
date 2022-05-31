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
import java.util.List;

import cs10.apps.common.android.CS_Fragment;
import cs10.apps.travels.tracer.DrawerActivity;
import cs10.apps.travels.tracer.Utils;
import cs10.apps.travels.tracer.adapter.LocatedArrivalsAdapter;
import cs10.apps.travels.tracer.databinding.FragmentHomeBinding;
import cs10.apps.travels.tracer.db.MiDB;
import cs10.apps.travels.tracer.model.Parada;
import cs10.apps.travels.tracer.model.Viaje;

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

        if (getActivity() instanceof DrawerActivity){
            DrawerActivity activity = (DrawerActivity) getActivity();
            activity.requestCurrentLocation().addOnSuccessListener(location -> {
                if (location != null) doInBackground(() -> onBuildHome(location));
            });
        }
    }

    public void onBuildHome(Location location){
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int m = calendar.get(Calendar.MINUTE);

        MiDB miDB = MiDB.getInstance(getContext());
        List<Parada> favourites = miDB.paradasDao().getFavouriteStops(hour, m);
        Utils.orderByProximity(favourites, location.getLatitude(), location.getLongitude());
        if (favourites.isEmpty()) return;
        List<Viaje> arrivals = miDB.viajesDao().getNextArrivals(favourites.get(0).getNombre(), hour, m);

        doInForeground(() -> {
            binding.tvTitle.setText("Próximos en " + favourites.get(0).getNombre());
            adapter.setViajes(arrivals);
            adapter.notifyDataSetChanged();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}