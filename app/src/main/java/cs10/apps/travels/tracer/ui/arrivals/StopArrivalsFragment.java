package cs10.apps.travels.tracer.ui.arrivals;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.LinkedList;

import cs10.apps.common.android.ui.CS_Fragment;
import cs10.apps.travels.tracer.R;
import cs10.apps.travels.tracer.adapter.LocatedArrivalAdapter;
import cs10.apps.travels.tracer.databinding.FragmentArrivalsBinding;
import cs10.apps.travels.tracer.model.Parada;
import cs10.apps.travels.tracer.ui.service.ServiceDetail;
import cs10.apps.travels.tracer.viewmodel.HomeVM;
import cs10.apps.travels.tracer.viewmodel.LocatedArrivalVM;
import cs10.apps.travels.tracer.viewmodel.LocationVM;
import cs10.apps.travels.tracer.viewmodel.RootVM;

public class StopArrivalsFragment extends CS_Fragment {
    private FragmentArrivalsBinding binding;
    private LocatedArrivalAdapter adapter;

    // ViewModel
    private LocatedArrivalVM locatedArrivalVM;
    private LocationVM locationVM;
    private HomeVM homeVM;
    private RootVM rootVM;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentArrivalsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new LocatedArrivalAdapter(new LinkedList<>(), true, arriboTren -> {
            onServiceSelected(arriboTren.getServiceId(), arriboTren.getRamal());
            return null;
        });

        binding.recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recycler.setAdapter(adapter);

        locatedArrivalVM = new ViewModelProvider(this).get(LocatedArrivalVM.class);
        locationVM = new ViewModelProvider(requireActivity()).get(LocationVM.class);
        homeVM = new ViewModelProvider(requireActivity()).get(HomeVM.class);
        rootVM = new ViewModelProvider(requireActivity()).get(RootVM.class);

        locatedArrivalVM.getStop().observe(getViewLifecycleOwner(), parada -> {
            // binding.pbar.setVisibility(View.VISIBLE);
            binding.tvTitle.setText(getString(R.string.next_ones_in, parada.getNombre()));
            locatedArrivalVM.recalculate(locationVM, homeVM);
            fillData(parada);
        });

        locatedArrivalVM.getStopZone().observe(getViewLifecycleOwner(), zone -> {
            if (zone == null) binding.tvSubtitle.setText(getString(R.string.unknown_zone));
            else binding.tvSubtitle.setText(zone.getName());
        });

        locatedArrivalVM.getGoingTo().observe(getViewLifecycleOwner(), goingTo -> {
            binding.walkingIcon.setVisibility(goingTo ? View.VISIBLE : View.GONE);
        });

        locatedArrivalVM.getArrivals().observe(getViewLifecycleOwner(), arrivals -> {
            int ogSize = adapter.getItemCount();
            adapter.setList(arrivals);

            if (ogSize == 0) adapter.notifyItemRangeInserted(0, arrivals.size());
            //else if (ogSize == adapter.getItemCount()) adapter.notifyItemRangeChanged(0, arrivals.size());
            else adapter.notifyDataSetChanged();

            // binding.pbar.setVisibility(View.GONE);
            rootVM.disableLoading();

            // OCT 2022: swipe
            new Handler(Looper.getMainLooper()).postDelayed(() -> binding.swipe.setRefreshing(false), 800);

        });

        locatedArrivalVM.getSummary().observe(getViewLifecycleOwner(), data -> {
            binding.stopSummary.travelCount.setText(getString(R.string.x_travels_done, data.component1()));
            binding.stopSummary.stopRank.setText(getString(R.string.number_x_in_ranking, data.component2()));
            binding.stopSummary.getRoot().setVisibility(View.VISIBLE);
        });

        locationVM.getLiveData().observe(getViewLifecycleOwner(), location -> {
            Double maxD = homeVM.getMaxDistance().getValue();
            if (maxD != null) locatedArrivalVM.recalculate(location.getLocation(), maxD);
        });

        // OCT 2022
        binding.swipe.setOnRefreshListener(() ->
            new Handler(Looper.getMainLooper()).postDelayed(() -> reload(true), 800)
        );
    }

    @Override
    public void onResume() {
        super.onResume();
        reload(false);
    }

    private void reload(boolean force){
        // get arguments
        Bundle args = getArguments();

        if (args != null) {
            int pos = args.getInt("pos");
            Parada parada = homeVM.getStop(pos);
            locatedArrivalVM.setStop(parada, force, rootVM);
        } else {
            Toast.makeText(requireContext(), "No se puede recargar", Toast.LENGTH_SHORT).show();
            binding.swipe.setRefreshing(false);
        }
    }

    private void fillData(Parada parada) {
        locatedArrivalVM.fillData(parada, rootVM, requireContext());
        locatedArrivalVM.calculateSummary(rootVM, parada.getNombre());
    }

    private void onServiceSelected(long id, String ramal) {
        Parada actual = locatedArrivalVM.getStop().getValue();
        if (actual == null) return;

        Intent intent = new Intent(getActivity(), ServiceDetail.class);
        intent.putExtra("station", actual.getNombre());
        intent.putExtra("ramal", ramal);
        intent.putExtra("id", id);
        startActivity(intent);
    }
}
