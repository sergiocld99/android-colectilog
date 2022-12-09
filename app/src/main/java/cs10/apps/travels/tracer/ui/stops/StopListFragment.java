package cs10.apps.travels.tracer.ui.stops;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import cs10.apps.common.android.CS_Fragment;
import cs10.apps.travels.tracer.Utils;
import cs10.apps.travels.tracer.adapter.LocatedStopAdapter;
import cs10.apps.travels.tracer.databinding.FragmentStopsBinding;
import cs10.apps.travels.tracer.db.MiDB;
import cs10.apps.travels.tracer.model.Parada;
import cs10.apps.travels.tracer.viewmodel.LocationVM;

public class StopListFragment extends CS_Fragment {
    private FragmentStopsBinding binding;
    private LocatedStopAdapter adapter;
    private MiDB miDB;

    // ViewModel
    private LocationVM locationVM;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentStopsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new LocatedStopAdapter(new LinkedList<>(), parada -> {
            onEditStop(parada.getNombre());
            return null;
        });

        binding.recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recycler.setAdapter(adapter);

        locationVM = new ViewModelProvider(requireActivity()).get(LocationVM.class);
    }

    private void recalc(Location location){
        if (adapter.getItemCount() == 0) fillData(location);
        else {
            for (Parada p : adapter.getParadasList()) p.updateDistance(location);
            Collections.sort(adapter.getParadasList());
            //Utils.orderByProximity(adapter.getParadasList(), location.getLatitude(), location.getLongitude());
            doInForeground(adapter::notifyDataSetChanged);
        }
    }

    private void fillData(Location location) {
        // locationVM.getLocation().removeObserver(firstLocationObserver);

        if (location != null){
            doInBackground(() -> {
                miDB = MiDB.getInstance(getContext());
                List<Parada> paradas = miDB.paradasDao().getAll();
                Utils.orderByProximity(paradas, location.getLatitude(), location.getLongitude());

                doInForeground(() -> {
                    adapter.setParadasList(paradas);
                    adapter.notifyItemRangeInserted(0, paradas.size());
                });
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        locationVM.getLiveData().observe(getViewLifecycleOwner(), it -> recalc(it.getLocation()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void onEditStop(String stopName) {
        Intent intent = new Intent(getActivity(), StopEditor.class);
        intent.putExtra("stopName", stopName);
        startActivity(intent);
    }
}